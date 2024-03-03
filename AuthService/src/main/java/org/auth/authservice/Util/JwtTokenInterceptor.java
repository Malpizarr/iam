package org.auth.authservice.Util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {


	private final TokenServiceClient tokenValidationService;

	public JwtTokenInterceptor(TokenServiceClient tokenValidationService) {
		this.tokenValidationService = tokenValidationService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String path = request.getRequestURI();
		String pattern = "/auth/(.+)/2fa/enable";
		String token = null;
		if (path.matches(pattern)) {
			if (request.getHeader("Authorization") != null) {
				token = request.getHeader("Authorization");
			} else {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals("AUTH_TOKEN")) {
							token = cookie.getValue();
						}
					}
				}
			} if (token != null) {
				if (token.startsWith("Bearer ")){
					token = token.substring(7);
				}
				String username = path.replaceAll(pattern, "$1");

				boolean isValid = tokenValidationService.validateToken(token, username);
				if (!isValid) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
					return false;
				}
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No Token Found");
				return false;
			}
		}
		return true;
	}
}