package org.auth.authservice.Controller;

import org.auth.authservice.Model.User;
import org.auth.authservice.Model.VerificationToken;
import org.auth.authservice.Repositories.VerificationTokenRepository;
import org.auth.authservice.Service.UserService;
import org.auth.authservice.Util.UserClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;

import java.util.Optional;

@RequestMapping("/verification")
@RestController
public class VerificationController {

	private final UserService userService;

	private final UserClientService userClientService;
	private final VerificationTokenRepository verificationTokenRepository;

	@Autowired
	public VerificationController(UserService userService, VerificationTokenRepository verificationTokenRepository, UserClientService userClientService) {
		this.userService = userService;
		this.verificationTokenRepository = verificationTokenRepository;
		this.userClientService = userClientService;

	}

	@GetMapping("/verify")
	public ResponseEntity<?> verifyEmail(@RequestParam String token) {
		Optional<VerificationToken> verificationTokenOpt = verificationTokenRepository.findByToken(token);

		if (verificationTokenOpt.isEmpty()) {
			return ResponseEntity.badRequest().body("Token inválido");
		}

		VerificationToken verificationToken = verificationTokenOpt.get();
		if (verificationToken.getExpiryDate().before(new Date())) {
			return ResponseEntity.badRequest().body("El token ha expirado");
		}

		String userId = verificationToken.getUserId();

		try {
			userClientService.verifyUser(userId);

			verificationTokenRepository.delete(verificationToken);

			return ResponseEntity.ok("Correo verificado con éxito");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al verificar el usuario: " + e.getMessage());
		}
	}


}
