package lt.ca.javau12.furnibay.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.controller.ContactRequestController;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.UserRepository;

@Service
public class ContactRequestService {

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectService projectService;
    
    private static final Logger logger = LoggerFactory.getLogger(ContactRequestService.class);

    // Sukuria naują kontaktinę užklausą
    public ContactRequest create(ContactRequest request) {
        request.setCreatedAt(LocalDate.now());
        return contactRequestRepository.save(request);
    }

    // Gauti visus ContactRequest
    public List<ContactRequest> getAll() {
        return contactRequestRepository.findAll();
    }

    
    // Gauti pagal ID
    public ContactRequest getById(Long id) {
        return contactRequestRepository.findById(id).orElse(null);
    }

    
    // Išsaugoti atnaujintą ContactRequest
    public ContactRequest save(ContactRequest request) {
        return contactRequestRepository.save(request);
    }

    
    @Transactional // Reikalingas kiekvienam public metodui
    public boolean deleteById(Long id) {
        return contactRequestRepository.findById(id)
            .map(request -> {
                // Pirmiausia atsieti projektą, jei jis yra
                if (request.getProject() != null) {
                    Project project = request.getProject();
                    project.setContactRequest(null);
                    projectRepository.save(project);
                }
                
                // Tada ištrinti užklausą
                contactRequestRepository.delete(request);
                
                return true;
            })
            .orElse(false);
    }


    // Atnaujinti statusą + sinchronizuoti su projektu
    @Transactional
    public ContactRequest updateStatus(Long contactRequestId, String newStatus) {
        ContactRequest request = contactRequestRepository.findById(contactRequestId)
            .orElseThrow(() -> new IllegalArgumentException("ContactRequest not found with ID: " + contactRequestId));

        request.setStatus(newStatus);
        ContactRequest savedRequest = contactRequestRepository.save(request);

        // Pakeista: naudojame tiesioginį metodą
        if (savedRequest.getProject() != null) {
            projectService.syncProjectStatusWithRequestStatus(savedRequest);
        }

        return savedRequest;
    }

    
    // Gauti projektą iš ContactRequest
    public Project getProjectFromContactRequest(Long contactRequestId) {
        ContactRequest request = contactRequestRepository.findById(contactRequestId)
            .orElseThrow(() -> new EntityNotFoundException("ContactRequest nerastas"));

        if (request.getProject() == null) {
            throw new EntityNotFoundException("Šis ContactRequest neturi susieto projekto");
        }

        return request.getProject();
    }

	/*
	 * @Transactional public Project convertContactToProject(Long contactId) {
	 * ContactRequest request = contactRequestRepository.findById(contactId)
	 * .orElseThrow(() -> new EntityNotFoundException("ContactRequest not found"));
	 * 
	 * if (request.getProject() != null) { return request.getProject(); }
	 * 
	 * Project project = new Project(); project.setName("Project from " +
	 * request.getName()); project.setDescription(request.getMessage());
	 * project.setStatus(request.getStatus() != null ? request.getStatus() : "New");
	 * project.setDeliveryDate(request.getDeliveryDate());
	 * project.setOrderPrice(request.getOrderPrice());
	 * project.setNotes(request.getNotes()); project.setCreatedAt(LocalDate.now());
	 * project.setContactRequest(request); project.setUser(request.getUser());
	 * 
	 * Project savedProject = projectRepository.save(project);
	 * 
	 * request.setProject(savedProject); request.setConvertedToProject(true);
	 * contactRequestRepository.save(request);
	 * 
	 * return savedProject; }
	 */
    
    @Transactional
    public Project convertContactToProject(Long contactId) {
        logger.debug("Attempting to convert contactId: {}", contactId);
        
        ContactRequest request = contactRequestRepository.findById(contactId)
            .orElseThrow(() -> {
                logger.error("ContactRequest not found for conversion: {}", contactId);
                return new EntityNotFoundException("ContactRequest not found");
            });
        
        if (request.isConvertedToProject()) {
            logger.warn("ContactRequest {} already converted to project {}", 
                contactId, request.getProject().getId());
            throw new IllegalStateException("Request already converted");
        }
        
        logger.info("Creating new project for contactId: {}", contactId);
        Project project = new Project();
        // ... konvertavimo logika
        
        request.setProject(project);
        request.setConvertedToProject(true);
        
        projectRepository.save(project);
        contactRequestRepository.save(request);
        
        logger.debug("Successfully created project {} for contactId {}", 
            project.getId(), contactId);
        
        return project;
    }
    
}