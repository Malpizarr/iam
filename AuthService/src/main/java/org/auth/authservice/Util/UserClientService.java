package org.auth.authservice.Util;

import org.auth.authservice.Model.User;
import org.auth.authservice.Model.VerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClientService {

	private final RestTemplate restTemplate;

	@Autowired
	public UserClientService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public User createUser(User newUser) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<User> request = new HttpEntity<>(newUser, headers);

			String url = "http://localhost:8083/users/CreateUser";

			ResponseEntity<User> response = restTemplate.postForEntity(url, request, User.class);

			// Verifica si la respuesta tiene un cuerpo y devuélvelo
			if (response.getBody() != null) {
				return response.getBody();
			} else {
				// Maneja el caso en el que la respuesta no tiene cuerpo, por ejemplo, lanzando una excepción
				throw new RuntimeException("La creación del usuario no devolvió ningún dato");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error creating user: " + e.getMessage(), e);
		}
	}


	public void verifyUser(String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<?> requestEntity = new HttpEntity<>(headers);

		String url = "http://localhost:8083/users/VerifyUser?Id=" + userId;

		try {
			ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

			System.out.println("Response status: " + response.getStatusCode());
		} catch (Exception e) {
			throw new RuntimeException("Error verifying user", e);
		}
	}

	public User getUser(User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Incluye el objeto User en el cuerpo de la solicitud
		HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

		String url = "http://localhost:8083/users/GetUser";

		try {
			// Realiza una solicitud POST en lugar de GET, ya que el endpoint espera un cuerpo de solicitud
			ResponseEntity<User> response = restTemplate.postForEntity(url, requestEntity, User.class);

			// Verifica si la respuesta tiene un cuerpo y devuélvelo
			if (response.getBody() != null) {
				return response.getBody();
			} else {
				throw new RuntimeException("La obtención del usuario no devolvió ningún dato");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error getting user: " + e.getMessage(), e);
		}
	}

	public byte[] enable2FAForUser(String username) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		String url = "http://localhost:8083/users/" + username + "/2fa/enable";

		try {
			ResponseEntity<byte[]> response = restTemplate.postForEntity(url, requestEntity, byte[].class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			String errorResponseBody = e.getResponseBodyAsString();
			throw new RuntimeException("Error enabling 2FA for user " + username + ": " + errorResponseBody, e);
		} catch (Exception e) {
			throw new RuntimeException("Error enabling 2FA for user " + username, e);
		}
	}


	public boolean verify2FA(String username, String verificationCode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		VerificationRequest request = new VerificationRequest(username, verificationCode);
		HttpEntity<VerificationRequest> requestEntity = new HttpEntity<>(request, headers);

		String url = "http://localhost:8083/users/verify-2fa";

		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		return response.getStatusCode() == HttpStatus.OK;
	}

}

