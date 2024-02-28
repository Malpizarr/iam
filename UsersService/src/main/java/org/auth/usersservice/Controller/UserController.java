package org.auth.usersservice.Controller;

import graphql.GraphQLContext;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import jakarta.servlet.http.HttpServletRequest;
import org.auth.usersservice.Model.User;
import org.auth.usersservice.Service.TokenValidationService;
import org.auth.usersservice.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private TokenValidationService tokenValidationService;

	@QueryMapping
	public List<User> users() {
		return userService.getAllUsers();
	}

	@QueryMapping
	public User user(@Argument String id) {
		return userService.getUserById(id);
	}

	@MutationMapping
	public User createUser( @Argument String username, @Argument String email, @Argument String password) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new GraphQLException("No JWT token found or token format is incorrect");
		}

		String token = authHeader.substring(7); // Extrae el token sin el prefijo "Bearer "

		if (!tokenValidationService.validateToken(token, username)) {
			throw new GraphQLException("Invalid or expired JWT token");
		}

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(password);
		userService.CreateUser(user);
		return user;
	}









	@MutationMapping
	public User updateUser(@Argument String id,@Argument String username,@Argument String email,@Argument String password) {
		User user = userService.getUserById(id);

		if (user == null) {
			throw new RuntimeException("User not found with ID: " + id);
		}

		if (username != null) {
			user.setUsername(username);
		}

		if (email != null) {
			user.setEmail(email);
		}

		if (password != null) {
			user.setPassword(password);
		}

		userService.save(user);
		return user;
	}

	@MutationMapping
	public Boolean deleteUser(@Argument String id) {
		User user = userService.getUserById(id);

		if (user == null) {
			throw new RuntimeException("User not found with ID: " + id);
		}

		userService.deleteUser(user);
		return true;
	}

	private void validateToken(String authHeader, String username) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new GraphQLException("No JWT token found or token format is incorrect");
		}

		String token = authHeader.substring(7); // Extrae el token sin el prefijo "Bearer "

		if (!tokenValidationService.validateToken(token, username)) {
			throw new GraphQLException("Invalid or expired JWT token");
		}
	}
}
