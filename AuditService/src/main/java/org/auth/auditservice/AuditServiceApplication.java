package org.auth.auditservice;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.auth.auditservice.grpc.AuditServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuditServiceApplication implements CommandLineRunner {

	@Value("${audit.grpc.port}")
	private int port;

	private final AuditServiceImpl auditServiceImpl;

	public AuditServiceApplication(AuditServiceImpl auditServiceImpl) {
		this.auditServiceImpl = auditServiceImpl;
	}

	public static void main(String[] args) {
		SpringApplication.run(AuditServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Server server = ServerBuilder.forPort(port)
				.addService(auditServiceImpl)
				.build();

		server.start();
		System.out.println("Server started on port " + port + "...");
		server.awaitTermination();
	}
}
