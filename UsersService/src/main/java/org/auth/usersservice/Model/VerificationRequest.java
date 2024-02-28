package org.auth.usersservice.Model;

public class VerificationRequest {
	private String username;
	private String verificationCode;

	public VerificationRequest(String username, String verificationCode) {
		this.username = username;
		this.verificationCode = verificationCode;
	}

	public String getUsername() {
		return username;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

}

