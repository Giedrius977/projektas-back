package lt.ca.javau12.furnibay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    // Add these methods:
    Optional<User> findByName(String name); // For username lookup
    
    @Query("SELECT u FROM User u WHERE u.name = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    @Query("SELECT p FROM Project p WHERE p.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);
}