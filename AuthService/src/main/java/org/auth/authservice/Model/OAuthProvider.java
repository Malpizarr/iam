//package org.auth.authservice.Model;
//
//import jakarta.persistence.*;
//
//@Entity
//public class OAuthProvider {
//
//	@Id
//	private String id;
//
//	private String providerId;
//
//	private String providerName;
//
//	@ManyToOne
//	@JoinColumn(name = "user_id")
//	private User user;
//
//
//	public String getId() {
//		return id;
//	}
//
//	public String getProviderId() {
//		return providerId;
//	}
//
//	public String getProviderName() {
//		return providerName;
//	}
//
//	public User getUser() {
//		return user;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public void setProviderId(String providerId) {
//		this.providerId = providerId;
//	}
//
//	public void setProviderName(String providerName) {
//		this.providerName = providerName;
//	}
//
//	public void setUser(User user) {
//		this.user = user;
//	}
//
//
//
//}
//
