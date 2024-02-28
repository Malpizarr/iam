package org.auth.usersservice.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenValidationService {

	private final RestTemplate restTemplate = new RestTemplate();
	private final String authServiceUrl = "http://localhost:8082/token/validate";

	public boolean validateToken(String token, String username) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, String> body = new HashMap<>();
		body.put("token", token);
		body.put("username", username);
		HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

		ResponseEntity<Boolean> response = restTemplate.postForEntity(
				"http://localhost:8082/token/validate", request, Boolean.class);

		return Boolean.TRUE.equals(response.getBody());
	}

}
