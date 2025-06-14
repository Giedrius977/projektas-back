package lt.ca.javau12.furnibay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserId(Long userId);
    
    Optional<Project> findByContactRequest(ContactRequest contactRequest);
    
    Optional<Project> findByContactRequestId(Long contactRequestId);
    
    List<Project> findByUser(User user);
    
    boolean existsByUser(User user);
    
    @Query("SELECT p FROM Project p JOIN p.user u WHERE u.email = :email")
    List<Project> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT new map(" +
    	       "p.id as id, " +
    	       "p.description as description, " +
    	       "p.status as status, " +
    	       "p.createdAt as createdAt, " +
    	       "p.deliveryDate as deliveryDate, " +
    	       "p.orderPrice as orderPrice, " +
    	       "p.notes as notes, " +
    	       "p.contactRequest.id as contactRequestId) " +
    	       "FROM Project p WHERE p.user.id = :userId")
    	List<Map<String, Object>> findSimpleProjectsByUserId(@Param("userId") Long userId);
}