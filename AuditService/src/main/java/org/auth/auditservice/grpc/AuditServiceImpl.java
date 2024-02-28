package org.auth.auditservice.grpc;


import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.auth.audit.AuditEvent;
import org.auth.audit.AuditServiceGrpc;
import org.auth.audit.LogResponse;
import org.auth.auditservice.Service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class AuditServiceImpl extends AuditServiceGrpc.AuditServiceImplBase {

	private final AuditService auditService;

	@Autowired
	public AuditServiceImpl(AuditService auditService) {
		this.auditService = auditService;
	}

	@Override
	public void logEvent(AuditEvent request, StreamObserver<LogResponse> responseObserver) {
		auditService.logEvent(request.getEventType(), request.getUsername(), request.getEventDateTime(), request.getDetails(), request.getIpAddress());

		LogResponse response = LogResponse.newBuilder().setSuccess(true).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
