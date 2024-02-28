package org.auth.usersservice.Model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
	private final String userId;

	private final String username;

	public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String userId) {
		super(username, password, authorities);
		this.userId = userId;
		this.username = username;
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}


}
