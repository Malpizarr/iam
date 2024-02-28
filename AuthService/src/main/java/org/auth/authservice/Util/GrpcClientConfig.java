package org.auth.authservice.Util;

import org.auth.authservice.grpc.AuditClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

	@Value("${audit.grpc.host}")
	private String host;

	@Value("${audit.grpc.port}")
	private int port;

	@Value("${user.grpc.host}")
	private String userHost;

	@Value("${user.grpc.port}")
	private int userPort;



	@Bean
	public AuditClient auditClient() {
		return new AuditClient(host, port);
	}

	@Bean
	public UserClient userClient() {
		return new UserClient(userHost, userPort);
	}
}
