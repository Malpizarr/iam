package org.auth.auditservice.Service;


import org.auth.auditservice.Model.AuditEvent;
import org.auth.auditservice.repositories.AuditEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

	private final AuditEventRepository auditEventRepository;

	@Autowired
	public AuditService(AuditEventRepository auditEventRepository) {
		this.auditEventRepository = auditEventRepository;
	}

	public AuditEvent logEvent(String eventType, String username, String eventDateTime, String details, String ipAddress) {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setEventType(eventType);
		auditEvent.setUsername(username);

		auditEvent.setEventDateTime(LocalDateTime.parse(eventDateTime));
		auditEvent.setDetails(details);
		auditEvent.setIpAddress(ipAddress);

		return auditEventRepository.save(auditEvent);
	}
}
