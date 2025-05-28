package lt.ca.javau12.furnibay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lt.ca.javau12.furnibay.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
