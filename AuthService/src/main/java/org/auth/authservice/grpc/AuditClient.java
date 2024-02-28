package org.auth.authservice.grpc;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.auth.authservice.AuditEvent;
import org.auth.authservice.AuditServiceGrpc;
import org.auth.authservice.LogResponse;




public class AuditClient {

	private final AuditServiceGrpc.AuditServiceBlockingStub auditServiceStub;

	public AuditClient(String host, int port) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
				.usePlaintext()
				.build();
		auditServiceStub = AuditServiceGrpc.newBlockingStub(channel);
	}



	public void logEvent(String eventType, String username, String eventDateTime, String details, String ipAddress) {
		AuditEvent event = AuditEvent.newBuilder()
				.setEventType(eventType)
				.setUsername(username)
				.setEventDateTime(eventDateTime)
				.setDetails(details)
				.setIpAddress(ipAddress)
				.build();

		LogResponse response = auditServiceStub.logEvent(event);
		System.out.println("Respuesta de registro de evento de auditoría: " + response.getSuccess());
	}
}
