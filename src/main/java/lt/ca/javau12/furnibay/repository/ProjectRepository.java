package lt.ca.javau12.furnibay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	
    List<Project> findByUserId(Long userId);
    
    Optional<Project> findByContactRequest(ContactRequest contactRequest);
    
    Optional<Project> findByContactRequestId(Long contactRequestId);
    
    List<Project> findByUser(User user);
    
    boolean existsByUser(User user);
    
    @Query("SELECT p FROM Project p JOIN p.user u WHERE u.email = :email")
    List<Project> findByUserEmail(@Param("email") String email);
}