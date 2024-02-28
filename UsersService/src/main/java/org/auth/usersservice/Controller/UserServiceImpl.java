package org.auth.usersservice.Controller;

import com.google.protobuf.ByteString;
import com.google.zxing.WriterException;
import io.grpc.Status;

import io.grpc.stub.StreamObserver;
import org.auth.user.*;
import org.auth.usersservice.Model.User;
import org.auth.usersservice.Service.TwoFactorAuthenticationService;
import org.auth.usersservice.Service.UserService;
import org.auth.usersservice.Util.TwoFaRequiredException;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;


@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

	private final UserService userService;
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	public UserServiceImpl(UserService userService, TwoFactorAuthenticationService twoFactorAuthenticationService) {
		this.userService = userService;
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
	}

	@Override
	public void createUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
		User user = convertToDomainUser(request.getUser());
		try {
			User userResponse = userService.CreateUser(user);
			UserResponse response = UserResponse.newBuilder()
					.setUser(convertToProtoUser(userResponse))
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (Exception e) {
			e.printStackTrace();
			responseObserver.onError(
					Status.INTERNAL
							.withDescription(e.getMessage())
							.asRuntimeException()
			);
		}
	}

	@Override
	public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
		User user = convertToDomainUser(request.getUser());
		try {
			User userResponse = userService.getUser(user);
			UserResponse response = UserResponse.newBuilder()
					.setUser(convertToProtoUser(userResponse))
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (TwoFaRequiredException e) {
			responseObserver.onError(
					Status.UNAUTHENTICATED
							.withDescription("2FA verification required")
							.asRuntimeException()
			);
		} catch (Exception e) {
			e.printStackTrace();
			responseObserver.onError(
					Status.INTERNAL
							.withDescription(e.getMessage())
							.asRuntimeException()
			);
		}
	}

	@Override
	public void verifyUser(VerifyUserRequest request, StreamObserver<VerifyUserResponse> responseObserver) {
		String id = request.getId();
		try {
			User user = userService.getUserById(id);
			userService.setVerified(user);
			VerifyUserResponse response = VerifyUserResponse.newBuilder()
					.setMessage("Correo verificado con éxito")
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (TwoFaRequiredException e) {
			responseObserver.onError(
					Status.UNAUTHENTICATED
							.withDescription("2FA verification required")
							.asRuntimeException()
			);
		} catch (Exception e) {
			e.printStackTrace();
			responseObserver.onError(
					Status.INTERNAL
							.withDescription(e.getMessage())
							.asRuntimeException()
			);
		}
	}

	@Override
	public void enable2FA(Enable2FARequest request, StreamObserver<Enable2FAResponse> responseObserver) {
		String username = request.getUsername();
		User user = userService.findByUsername(username);
		if (user == null) {
			responseObserver.onError(
					Status.NOT_FOUND
							.withDescription("User not found")
							.asRuntimeException()
			);
			return;
		}
		if (user.isTwoFaEnabled()) {
			responseObserver.onError(
					Status.ALREADY_EXISTS
							.withDescription("2FA is already enabled")
							.asRuntimeException()
			);
			return;
		}

		String secretKey = twoFactorAuthenticationService.generateSecretKey();
		user.setTotpSecret(secretKey);
		user.setTwoFaEnabled(true);

		String otpAuthUrl = twoFactorAuthenticationService.generateTotpUrl(secretKey, "IAM", user.getEmail());
		byte[] qrCode;
		try {
			qrCode = twoFactorAuthenticationService.generateQrCode(otpAuthUrl);
		} catch (IOException | WriterException e) {
			e.printStackTrace();
			responseObserver.onError(
					Status.INTERNAL
							.withDescription(e.getMessage())
							.asRuntimeException()
			);
			return;
		}

		userService.save(user);

		Enable2FAResponse response = Enable2FAResponse.newBuilder()
				.setQrCode(ByteString.copyFrom(qrCode))
				.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void verify2FA(Verify2FARequest request, StreamObserver<Verify2FAResponse> responseObserver) {
		String username = request.getUsername();
		String verificationCode = request.getVerificationCode();
		User user = userService.findByUsername(username);
		if (user == null) {
			responseObserver.onError(
					Status.NOT_FOUND
							.withDescription("User not found")
							.asRuntimeException()
			);
			return;
		}

		boolean isCodeValid = twoFactorAuthenticationService.verifyCode(verificationCode, user.getTotpSecret());

		if (!isCodeValid) {
			Verify2FAResponse response = Verify2FAResponse.newBuilder()
					.setSuccess(false)
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			return;
		}

		// Si el código es válido, envía una respuesta con el campo 'success' establecido en true.
		Verify2FAResponse response = Verify2FAResponse.newBuilder()
				.setSuccess(true)
				.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}


	@Override
	public void processOAuthUser(ProcessOAuthUserRequest request, StreamObserver<ProcessOAuthUserResponse> responseObserver) {
		Map<String, String> userDetails = request.getUserDetailsMap();
		String email = userDetails.getOrDefault("email", "");
		String name = userDetails.getOrDefault("name", "");
		String provider = userDetails.getOrDefault("provider", "");
		String sub = userDetails.getOrDefault("sub", "");

		User user = userService.findOrCreateUser(email, name, provider, sub);

		org.auth.usersservice.Model.OauthDTO userDto = userService.convertToDto(user);

		org.auth.user.OauthDTO grpcUserDto = org.auth.user.OauthDTO.newBuilder()
				.setEmail(userDto.getEmail())
				.setName(userDto.getName())
				.setProvider(userDto.getProvider())
				.setSub(userDto.getSub())
				.build();

		ProcessOAuthUserResponse response = ProcessOAuthUserResponse.newBuilder()
				.setOauthDTO(grpcUserDto)
				.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}


	private User convertToDomainUser(UserProto userProto) {
		User user = new User();
		user.setUsername(userProto.getUsername());
		user.setEmail(userProto.getEmail());
		user.setPassword(userProto.getPassword());
		user.setTwoFaEnabled(userProto.getIsTwoFaEnabled());
		user.setTotpSecret(userProto.getTotpSecret());
		return user;
	}

	private UserProto convertToProtoUser(User user) {
		UserProto.Builder userProto = UserProto.newBuilder();
		userProto.setId(user.getId());
		userProto.setUsername(user.getUsername());
		userProto.setEmail(user.getEmail());
		userProto.setPassword(user.getPassword());
		userProto.setIsTwoFaEnabled(user.isTwoFaEnabled());
		if (user.getTotpSecret() != null && !user.getTotpSecret().isEmpty()){
			userProto.setTotpSecret(user.getTotpSecret());
		}else {
			userProto.setTotpSecret("");
		}
		return userProto.build();
	}



}
