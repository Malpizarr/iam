package org.auth.usersservice.Controller;

import graphql.GraphQLException;
import jakarta.servlet.http.HttpServletRequest;
import org.auth.usersservice.Model.User;
import org.auth.usersservice.Service.TokenValidationService;
import org.auth.usersservice.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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

		String token = authHeader.substring(7);

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

}
