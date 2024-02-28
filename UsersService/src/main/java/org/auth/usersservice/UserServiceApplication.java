package org.auth.usersservice;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.auth.usersservice.Controller.UserServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication implements CommandLineRunner {

	@Value("${user.grpc.port}")
	private int port;

	private final UserServiceImpl userServiceImpl;

	public UserServiceApplication(UserServiceImpl userServiceImpl) {
		this.userServiceImpl = userServiceImpl;
	}

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Server server = ServerBuilder.forPort(port)
				.addService(userServiceImpl)
				.build();

		server.start();
		System.out.println("Server started on port " + port + "...");
		server.awaitTermination();
	}
}
