package org.auth.authservice.Util;

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

		if (path.matches(pattern)) {
			String token = request.getHeader("Authorization");
			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);

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