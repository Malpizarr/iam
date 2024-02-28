package org.auth.tokenservice.Controller;

import org.auth.tokenservice.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/token")
public class TokenController {

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/generate")
	public ResponseEntity<?> generateToken(@RequestBody String username) {
		String token = jwtUtil.generateToken(username);
		return ResponseEntity.ok(token);
	}

	@PostMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestBody Map<String, String> payload) {
		String token = payload.get("token");
		String username = payload.get("username");
		boolean isValid = jwtUtil.validateToken(token, username);
		return ResponseEntity.ok(isValid);
	}

}
