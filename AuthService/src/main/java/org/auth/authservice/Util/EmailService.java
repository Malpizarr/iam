package org.auth.authservice.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	public void sendEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("tuCorreo@gmail.com");
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		mailSender.send(message);
	}

	public void sendVerificationEmail(String to, String verificationURL) {
		String subject = "Verificación de Correo Electrónico";
		String text = "Por favor, verifica tu correo haciendo clic en el siguiente enlace: " + verificationURL;
		sendEmail(to, subject, text);
	}

	public void sendInvalidPasswordEmail(String email) {
		String subject = "Contraseña Inválida";
		String text = "La contraseña que ingresaste es inválida. Por favor, intenta nuevamente.";
		sendEmail(email, subject, text);
	}

	public void sendLoginEmail(String email, String ipAddress) {
		String subject = "Inicio de Sesión";
		String text = "Se ha iniciado sesión en tu cuenta desde" + ipAddress + ". Si no fuiste tú, por favor, contacta a soporte.";
		sendEmail(email, subject, text);
	}
}
