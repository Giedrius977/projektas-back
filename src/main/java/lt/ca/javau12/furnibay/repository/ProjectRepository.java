package lt.ca.javau12.furnibay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import lt.ca.javau12.furnibay.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	
    List<Project> findByUserId(Long userId);
    
    List<Project> findByClientUsername(String clientUsername);

    
}
