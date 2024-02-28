package org.auth.authservice.Service;

import com.google.zxing.WriterException;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.jboss.aerogear.security.otp.api.Clock;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TwoFactorAuthenticationService {

	public String generateSecretKey() {
		return Base32.random();
	}

	public boolean verifyCode(String userCode, String secretKey) {
		Totp totp = new Totp(secretKey);
		return totp.verify(userCode);
	}

	public String generateTotpUrl(String secretKey, String issuer, String account) {
		return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
				URLEncoder.encode(issuer, StandardCharsets.UTF_8),
				URLEncoder.encode(account, StandardCharsets.UTF_8),
				URLEncoder.encode(secretKey, StandardCharsets.UTF_8),
				URLEncoder.encode(issuer, StandardCharsets.UTF_8));
	}

	public byte[] generateQrCode(String totpUrl) throws IOException, WriterException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(totpUrl, BarcodeFormat.QR_CODE, 200, 200);
		ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
		return pngOutputStream.toByteArray();
	}
}
