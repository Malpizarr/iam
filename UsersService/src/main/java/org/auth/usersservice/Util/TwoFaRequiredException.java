package org.auth.usersservice.Util;

public class TwoFaRequiredException extends RuntimeException {
	public TwoFaRequiredException(String message) {
		super(message);
	}
}
