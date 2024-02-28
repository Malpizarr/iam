package org.auth.authservice.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.auth.authservice.Model.EmailInfo;
import org.auth.authservice.Model.User;

import org.auth.authservice.Util.UserClient;
import org.auth.authservice.grpc.AuditClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Lazy
	private PasswordEncoder passwordEncoder;

	@Autowired
	@Lazy
	private UserService userService;

	@Autowired
	private  AuditClient auditClient;

	@Autowired
	private UserClient userClient;


	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		String providerName = userRequest.getClientRegistration().getRegistrationId();
		return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "name");
	}

	@Transactional
	public DefaultOAuth2User processUserDetails(OAuth2User oAuth2User, String providerName, String accessToken, String name, String ipaddress) throws JsonProcessingException {
		Map<String, Object> attributes = oAuth2User.getAttributes();
		String sub = providerName.equals("github") ? attributes.get("id").toString() : oAuth2User.getName();
		String email = providerName.equals("github") ? fetchEmailFromGitHub(accessToken) : (String) attributes.get("email");

		User user = userClient.processOAuthUser(email, name, providerName, sub);

		Map<String, Object> userAttributes = new HashMap<>(oAuth2User.getAttributes());
		userAttributes.put("email", user.getEmail());

		auditClient.logEvent("LOGIN", name, LocalDateTime.now().toString(), "User LOGGED IN successfully w/ OAUTH", ipaddress);

		return new DefaultOAuth2User(oAuth2User.getAuthorities(), userAttributes, "email");
	}


	private String fetchEmailFromGitHub(String accessToken) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "token " + accessToken);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		ResponseEntity<EmailInfo[]> response = restTemplate.exchange(
				"https://api.github.com/user/emails", HttpMethod.GET, entity, EmailInfo[].class);

		if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
			for (EmailInfo emailInfo : response.getBody()) {
				if (emailInfo.isPrimary()) {
					return emailInfo.getEmail();
				}
			}
		}
		return null;
	}



}
