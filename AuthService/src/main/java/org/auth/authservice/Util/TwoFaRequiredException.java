package org.auth.authservice.Util;

public class TwoFaRequiredException extends RuntimeException {
	public TwoFaRequiredException(String message) {
		super(message);
	}
}
