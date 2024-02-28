package org.auth.tokenservice.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	private Key key;

	@PostConstruct
	public void init() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
	}

	public String generateToken(String username) {
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Boolean validateToken(String token, String username) {
		final String usernameInToken = getUsernameFromToken(token);
		return (username.equals(usernameInToken) && !isTokenExpired(token));
	}

	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();

		String subjectJson = claims.getSubject();
		ObjectMapper mapper = new ObjectMapper();

		try {
			Map<String, String> subjectMap = mapper.readValue(subjectJson, Map.class);
			return subjectMap.get("username");
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error parsing username from token", e);
		}
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}
