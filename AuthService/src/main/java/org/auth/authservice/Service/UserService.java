package org.auth.authservice.Service;

import org.auth.authservice.Model.CustomUserDetails;
//import org.auth.authservice.Model.Role;
import org.auth.authservice.Model.User;
import org.auth.authservice.Model.VerificationToken;
//import org.auth.authservice.Repositories.RoleRepository;
//import org.auth.authservice.Repositories.UserRepository;
import org.auth.authservice.Repositories.VerificationTokenRepository;
import org.auth.authservice.Util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.regex.Pattern;


@Service
public class UserService {


	private final PasswordEncoder passwordEncoder;

	private final EmailService emailService;

	private final UserClientService userClientService;

	private final VerificationTokenRepository verificationTokenRepository;

	private final UserClient userClient;

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
	);

	@Autowired
	public UserService(PasswordEncoder passwordEncoder, EmailService emailService, VerificationTokenRepository verificationTokenRepository, UserClientService userClientService, UserClient userClient) {
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
		this.verificationTokenRepository = verificationTokenRepository;
		this.userClientService = userClientService;
		this.userClient = userClient;
	}

	public User register(User newUser) {
		try {

			User user = userClient.createUser(newUser.getUsername(), newUser.getEmail(), newUser.getPassword());

			String token = UUID.randomUUID().toString();
			VerificationToken verificationToken = new VerificationToken(token, user.getId());
			verificationTokenRepository.save(verificationToken);

			String verificationUrl = "http://localhost:8080/verification/verify?token=" + token;
			emailService.sendVerificationEmail(newUser.getEmail(), verificationUrl);

			return user;
		}catch(Exception e){
			throw new RuntimeException("Error creating user");
		}


	}

	public User login(User user) {
		User userResponse = userClient.getUser(user);
		if (userResponse == null) {
			throw new UsernameNotFoundException("User not found");
		}
		if (!passwordEncoder.matches(user.getPassword(), userResponse.getPassword())) {
			System.out.println(passwordEncoder.matches(user.getPassword(), userResponse.getPassword()));
			throw new RuntimeException("Invalid password");
		}

		return userResponse;
	}


	public byte[] enable2FAForUser(String username) throws Exception {
		byte[] qrCode = userClient.enable2FAForUser(username);
		return qrCode;
	}

	public boolean verify2FA(String username, String verificationCode) {
		return userClient.verify2FAForUser(username, verificationCode);
	}
}