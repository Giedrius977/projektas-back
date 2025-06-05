package lt.ca.javau12.furnibay.repository;

import lt.ca.javau12.furnibay.dto.ContactRequest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
	
	List<ContactRequest> findByConvertedToProjectFalse();
	
    Optional<ContactRequest> findById(Long id);
    

}

