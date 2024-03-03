package org.auth.usersservice.Model;

import jakarta.persistence.*;

import java.util.*;


@Entity
@Table(name = "user")
public class User {

	@Id
	private String id;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(unique = true, nullable = false)
	private String email;

	@Column
	private String totpSecret;

	@Column
	private Boolean isTwoFaEnabled = false;

	@Column
	private Boolean isEmailVerified = false;



	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	private Set<Role> roles = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<OAuthProvider> providers = new HashSet<>();

	public User() {
	}
	@PrePersist
	protected void onCreate() {

		if (id == null) {
			id = UUID.randomUUID().toString();
		}
	}
	public Boolean getEmailVerified() {
		return isEmailVerified;
	}
	public void setEmailVerified(Boolean isEmailVerified) {
		this.isEmailVerified = isEmailVerified;
	}
	public String getId() {
		return id;
	}

	public String getTotpSecret() {
		return totpSecret;
	}

	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}

	public Boolean isTwoFaEnabled() {
		return isTwoFaEnabled;
	}

	public void setTwoFaEnabled(Boolean isTwoFaEnabled) {
		this.isTwoFaEnabled = isTwoFaEnabled;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}

	public void addProvider(OAuthProvider provider) {
		providers.add(provider);
		provider.setUser(this);
	}

	public void removeProvider(OAuthProvider provider) {
		providers.remove(provider);
		provider.setUser(null);
	}

	public Set<OAuthProvider> getProviders() {
		return providers;
	}

	public void setProviders(Set<OAuthProvider> providers) {
		this.providers = providers;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
}
