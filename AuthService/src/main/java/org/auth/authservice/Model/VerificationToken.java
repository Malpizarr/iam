package org.auth.authservice.Model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
public class VerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String token;

	private Date expiryDate;

	// Cambia la relación directa de User a solo almacenar el userId
	@Column(name = "user_id")
	private String userId;

	public VerificationToken() {
	}

	public VerificationToken(String token, String userId) {
		this.token = token;
		this.userId = userId;
		this.expiryDate = calculateExpiryDate();
	}

	private Date calculateExpiryDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Timestamp(cal.getTime().getTime()));
		cal.add(Calendar.MINUTE, 180); // Expira en 180 minutos / 3 horas
		return new Date(cal.getTime().getTime());
	}

	// Getters y setters
	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public String getUserId() {
		return userId;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
}
