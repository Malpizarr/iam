package org.auth.authservice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.auth.authservice.Model.User;
import org.auth.authservice.Service.CustomOAuth2UserService;
import org.auth.authservice.Util.JwtTokenInterceptor;
import org.auth.authservice.Util.TokenServiceClient;
import org.auth.authservice.Util.TwoFaRequiredException;
import org.auth.authservice.grpc.AuditClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomOAuth2UserService CustomOAuth2UserService;

	@Autowired
	TokenServiceClient tokenServiceClient;

	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;




	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeRequests(authz -> authz
						.requestMatchers("/auth/**", "/error", "/oauth2/**", "/uploads/**", "/verification/verify").permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(form -> form
						.loginPage("/login")
						.permitAll()
				)
				.logout(LogoutConfigurer::permitAll)
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(userInfo -> userInfo.userService(CustomOAuth2UserService))
						.successHandler(authenticationSuccessHandler())
				);

		return http.build();
	}

	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return new AuthenticationSuccessHandler() {
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
				OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
				String providerName = authToken.getAuthorizedClientRegistrationId();
				OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
				String name = providerName.equals("github") ? oAuth2User.getAttribute("login") : oAuth2User.getAttribute("name");
				String ipAddress = request.getRemoteAddr();
				if (ipAddress == null) {
					ipAddress = request.getHeader("X-Forwarded-For");
				}
				try {
					OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
							authToken.getAuthorizedClientRegistrationId(), authToken.getName());
					String accessToken = client.getAccessToken().getTokenValue();
					DefaultOAuth2User user = CustomOAuth2UserService.processUserDetails(oAuth2User, providerName, accessToken, name, ipAddress);
					String token = tokenServiceClient.generateToken(name);
					System.out.println("Token: " + token);
					response.setStatus(HttpServletResponse.SC_OK);
				} catch (TwoFaRequiredException e) {
					if (name != null) {
						String encodedUsername = URLEncoder.encode(name, StandardCharsets.UTF_8);
						String twoFaPageUrl = "http://127.0.0.1:3000/path-to-2fa-page.html?username=" + encodedUsername;
						response.sendRedirect(twoFaPageUrl);
					} else {
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Username is not available");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Configuration
	public static class WebConfig implements WebMvcConfigurer {

		@Autowired
		private JwtTokenInterceptor twoFaInterceptor;

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(twoFaInterceptor)
					.addPathPatterns("/auth/*/2fa/enable");
		}

	}

	@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://127.0.0.1:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }

	}

