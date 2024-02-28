package org.auth.authservice.Util;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.auth.authservice.Model.User;
import org.auth.user.*;

import java.util.HashMap;
import java.util.Map;


public class UserClient {

	private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

	public UserClient(String host, int port) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
				.usePlaintext()
				.build();

		userServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel);
	}

	public User createUser(String username, String email, String password) {
		UserProto userProto = UserProto.newBuilder()
				.setUsername(username)
				.setEmail(email)
				.setPassword(password)
				.build();

		UserRequest request = UserRequest.newBuilder()
				.setUser(userProto)
				.build();

		try {
			UserResponse response = userServiceBlockingStub.createUser(request);
			System.out.println("CreateUser response: " + response.getUser());
			return convertToDomainUser(response.getUser());
		} catch (StatusRuntimeException e) {
			System.err.println("RPC failed: " + e.getStatus());
			return null;
		}
	}

	public User getUser(User user) {
		UserProto userProtoRequest = UserProto.newBuilder()
				.setUsername(user.getUsername())
				.setPassword(user.getPassword())
				.build();

		UserRequest userRequest = UserRequest.newBuilder()
				.setUser(userProtoRequest)
				.build();



		try {
			UserResponse response = userServiceBlockingStub.getUser(userRequest);
			System.out.println("GetUser response: " + response.getUser());
			return convertToDomainUser(response.getUser());
		} catch (StatusRuntimeException e) {
			System.err.println("RPC failed: " + e.getStatus());
			return null;
		}
	}

	public void verifyUser(String userId) {
		VerifyUserRequest request = VerifyUserRequest.newBuilder()
				.setId(userId)
				.build();

		try {
			VerifyUserResponse response = userServiceBlockingStub.verifyUser(request);
			System.out.println("VerifyUser response: " + response.getMessage());
		} catch (StatusRuntimeException e) {
			System.err.println("RPC failed: " + e.getStatus());
		}
	}

	public byte[] enable2FAForUser(String username) throws Exception {
		Enable2FARequest request = Enable2FARequest.newBuilder().setUsername(username).build();

		Enable2FAResponse response = userServiceBlockingStub.enable2FA(request);

		return response.getQrCode().toByteArray();
	}

	public boolean verify2FAForUser(String username, String code) {
		Verify2FARequest request = Verify2FARequest.newBuilder()
				.setUsername(username)
				.setVerificationCode(code)
				.build();

		Verify2FAResponse response = userServiceBlockingStub.verify2FA(request);

		return response.getSuccess();
	}

	public User processOAuthUser(String email, String name, String provider, String sub) {
		// Construir el mapa de detalles del usuario
		Map<String, String> userDetailsMap = new HashMap<>();
		userDetailsMap.put("email", email);
		userDetailsMap.put("name", name);
		userDetailsMap.put("provider", provider);
		userDetailsMap.put("sub", sub);

		ProcessOAuthUserRequest request = ProcessOAuthUserRequest.newBuilder()
				.putAllUserDetails(userDetailsMap)
				.build();

		try {
			ProcessOAuthUserResponse response = userServiceBlockingStub.processOAuthUser(request);

			org.auth.user.OauthDTO oauthDTO = response.getOauthDTO();
			User user = new User();
			user.setEmail(oauthDTO.getEmail());
			user.setUsername(oauthDTO.getName());
			return user;
		} catch (StatusRuntimeException e) {
			System.err.println("RPC failed: " + e.getStatus());
			throw new RuntimeException("Failed to process OAuth user");
		}
	}




	private User convertToDomainUser(UserProto userProto) {
		User user = new User();
		user.setId(userProto.getId());
		user.setUsername(userProto.getUsername());
		user.setEmail(userProto.getEmail());
		user.setPassword(userProto.getPassword());
		return user;
	}



}
