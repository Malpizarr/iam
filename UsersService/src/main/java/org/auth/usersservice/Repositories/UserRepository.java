package org.auth.usersservice.Repositories;

import org.auth.usersservice.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findById (String id);
	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);



}
