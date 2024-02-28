package org.auth.authservice.Controller;


import com.google.zxing.WriterException;
import jakarta.servlet.http.HttpServletRequest;
import org.auth.authservice.Model.User;
import org.auth.authservice.Model.VerificationRequest;
import org.auth.authservice.Service.TwoFactorAuthenticationService;
import org.auth.authservice.Service.UserService;
import org.auth.authservice.Util.EmailService;
import org.auth.authservice.Util.TokenServiceClient;
import org.auth.authservice.Util.TwoFaRequiredException;
import org.auth.authservice.grpc.AuditClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserService userService;
	private final EmailService emailService;
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private final TokenServiceClient tokenServiceClient;
	private final AuditClient auditClient;

	@Autowired
	public AuthController(UserService userService, EmailService emailService, TwoFactorAuthenticationService twoFactorAuthenticationService, TokenServiceClient tokenServiceClient, AuditClient auditClient) {
		this.userService = userService;
		this.emailService = emailService;
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
		this.tokenServiceClient = tokenServiceClient;
		this.auditClient = auditClient;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody User newUser,HttpServletRequest request) {
		try {
			String ipAddress = request.getHeader("X-Forwarded-For");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}

			User user = userService.register(newUser);
			auditClient.logEvent("REGISTER", user.getUsername(), LocalDateTime.now().toString(), "User registered successfully", ipAddress);
			return ResponseEntity.ok(user);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

		}
	}
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user, HttpServletRequest request) {
		try {
			User userResponse = userService.login(user);
			String token = tokenServiceClient.generateToken(userResponse.getUsername());

			String ipAddress = request.getHeader("X-Forwarded-For");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}
			auditClient.logEvent("LOGIN", userResponse.getUsername(), LocalDateTime.now().toString(), "User logged in successfully from IP: " + ipAddress, ipAddress);

			emailService.sendLoginEmail(userResponse.getEmail(), ipAddress);
			return ResponseEntity.ok().body(token);
		} catch (TwoFaRequiredException e) {
			String ipAddress = request.getHeader("X-Forwarded-For");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}
			auditClient.logEvent("LOGIN_FAILED", user.getUsername(), LocalDateTime.now().toString(), "2FA required from IP: " + ipAddress, ipAddress);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("2FA verification required");
		} catch (Exception e) {
			e.printStackTrace();
			String ipAddress = request.getHeader("X-Forwarded-For");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}
			auditClient.logEvent("LOGIN_FAILED", user.getUsername(), LocalDateTime.now().toString(), e.getMessage() + " from IP: " + ipAddress, ipAddress);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}




	@PostMapping("/verify-2fa")
	public ResponseEntity<?> verify2FA(@RequestBody VerificationRequest request) {
		try {
			boolean verificationResult = userService.verify2FA(request.getUsername(), request.getVerificationCode());
			if (!verificationResult) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");
			}
			String token = tokenServiceClient.generateToken(request.getUsername());
			return ResponseEntity.ok().body(token);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during 2FA verification");
		}
	}


	@PostMapping("/{username}/2fa/enable")
	public ResponseEntity<?> enable2FA(@PathVariable String username) {
		try {
			byte[] qrCode = userService.enable2FAForUser(username);
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_PNG)
					.body(qrCode);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error enabling 2FA: " + e.getMessage());
		}
	}
}
