package org.auth.usersservice.Service;

import org.auth.usersservice.Model.*;
import org.auth.usersservice.Repositories.RoleRepository;
import org.auth.usersservice.Repositories.UserRepository;
import org.auth.usersservice.Util.PasswordGenerator;
import org.auth.usersservice.Util.TwoFaRequiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;


@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final RoleRepository roleRepository;


	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
	);

	@Autowired
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		return new CustomUserDetails(user.getUsername(), user.getPassword(), new ArrayList<>(),  user.getId());
	}

	public User CreateUser(User newUser) {
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("Role not found"));

		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		newUser.setRoles(roles);
		if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
			throw new RuntimeException("Username already taken");
		}

		if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
			throw new RuntimeException("Email already taken");
		}

		if (!EMAIL_PATTERN.matcher(newUser.getEmail()).matches()) {
			throw new RuntimeException("Invalid email format");
		}

		newUser.setTwoFaEnabled(false);
		newUser.setEmailVerified(false);
		newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

		userRepository.save(newUser);

		return newUser;
	}


	public void setVerified(User user) {
		user.setEmailVerified(true);
		userRepository.save(user);
	}


	public User getUser(User user) {
		User user1 = userRepository.findByUsername(user.getUsername())
				.or(() -> userRepository.findByEmail(user.getEmail()))
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (user1.isTwoFaEnabled()) {
			throw new TwoFaRequiredException("2FA verification required");
		}

		return user1;
	}



	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.or(() -> userRepository.findByEmail(username))
				.orElseThrow(() -> new RuntimeException("User not found"));
	}


	public User findOrCreateUser(String email, String name, String providerName, String sub) {
		Optional<User> existingUser = userRepository.findByEmail(email);

		if (existingUser.isPresent()) {
			User user = existingUser.get();
			boolean providerExists = user.getProviders().stream().anyMatch(p -> p.getProviderName().equals(providerName));
			if (!providerExists) {
				OAuthProvider provider = new OAuthProvider();
				provider.setId(sub + providerName);
				provider.setUser(user);
				provider.setProviderId(sub);
				provider.setProviderName(providerName);
				user.addProvider(provider);
				userRepository.save(user);
			}
			if (user.isTwoFaEnabled()) {
				throw new TwoFaRequiredException("2FA verification required");
			}
			return user;
		}

		User newUser = new User();
		newUser.setEmail(email);
		newUser.setUsername(name);
		newUser.setPassword(passwordEncoder.encode(PasswordGenerator.generatePassword()));
		CreateUser(newUser);


		boolean providerExists = newUser.getProviders().stream().anyMatch(p -> p.getProviderName().equals(providerName));
		if (!providerExists) {
			OAuthProvider provider = new OAuthProvider();
			provider.setId(sub + providerName);
			provider.setUser(newUser);
			provider.setProviderId(sub);
			provider.setProviderName(providerName);
			newUser.addProvider(provider);
			userRepository.save(newUser);
		}

		return userRepository.save(newUser);
	}



	public OauthDTO convertToDto(User user) {
		OauthDTO dto = new OauthDTO();
		dto.setEmail(user.getEmail());
		dto.setName(user.getUsername());
		dto.setProvider(user.getProviders().iterator().next().getProviderName());
		dto.setSub(user.getProviders().iterator().next().getProviderId());
		return dto;
	}


	public void save(User user) {
		userRepository.save(user);
	}

	public User getUserById(String id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	public void deleteUser(User user) {
		userRepository.delete(user);
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
}