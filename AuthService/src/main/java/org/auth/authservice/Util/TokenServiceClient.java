package org.auth.authservice.Util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenServiceClient {

	private final RestTemplate restTemplate;


	public TokenServiceClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

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



	public String generateToken(String username) {
		// Crear el cuerpo de la solicitud con el username
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("username", username);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Envolver el cuerpo y los encabezados en una entidad HttpEntity
		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		// Realizar la solicitud POST al endpoint de generación de tokens
		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:8082/token/generate",
				request,
				String.class
		);

		// Verificar el estado de la respuesta y retornar el token
		if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
			return response.getBody(); // El cuerpo de la respuesta contiene el token
		} else {
			throw new RuntimeException("Failed to generate token");
		}
	}
}
