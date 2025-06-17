package lt.ca.javau12.furnibay.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.exception.ResourceNotFoundException;
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
    public ContactRequestService(ContactRequestRepository contactRequestRepository,
                               ProjectRepository projectRepository) {
        this.contactRequestRepository = contactRequestRepository;
        this.projectRepository = projectRepository;
    }
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectService projectService;
    
    private static final Logger logger = LoggerFactory.getLogger(ContactRequestService.class);

    
    public ContactRequestService(ContactRequestRepository contactRequestRepository) {
        this.contactRequestRepository = contactRequestRepository;
    }
    
    @Transactional(readOnly = true)
    public ContactRequest findById(Long id) {
        Optional<ContactRequest> contactRequest = contactRequestRepository.findById(id);
        if (contactRequest.isEmpty()) {
            throw new ResourceNotFoundException("Contact request not found with id: " + id);
        }
        return contactRequest.get();
    }
    
    
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

    
    @Transactional
    public boolean deleteById(Long id) {
        try {
            contactRequestRepository.deleteById(id);
            contactRequestRepository.flush();
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
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

    @Transactional
    public Project convertContactToProject(Long contactId) {
        logger.debug("Attempting to convert contactId: {}", contactId);

        // Tikriname ar jau yra sukurtas projektas pagal contactId
        return projectRepository.findByContactRequestId(contactId)
            .orElseGet(() -> {
                // Užklausos gavimas
                ContactRequest request = contactRequestRepository.findById(contactId)
                    .orElseThrow(() -> {
                        logger.error("ContactRequest not found for conversion: {}", contactId);
                        return new EntityNotFoundException("ContactRequest not found");
                    });

                if (request.isConvertedToProject() || request.getProject() != null) {
                    logger.warn("ContactRequest {} already converted to project {}", 
                        contactId, request.getProject() != null ? request.getProject().getId() : "N/A");
                    throw new IllegalStateException("Request already converted");
                }

                logger.info("Creating new project for contactId: {}", contactId);

                // Naujo projekto kūrimas
                Project project = new Project();
                project.setName("Project from " + request.getName());

                // Užtikriname, kad description niekada nebus null
                String description = request.getMessage() != null ?
                    (request.getMessage().length() > 200 ?
                        request.getMessage().substring(0, 200) :
                        request.getMessage()) :
                    "Projektas iš užklausos #" + contactId;

                project.setDescription(description);
                project.setStatus("Projektuojama");
                project.setDeliveryDate(request.getDeliveryDate());
                project.setOrderPrice(request.getOrderPrice());
                project.setNotes(request.getNotes());
                project.setContactRequest(request);

                // Išsaugome projektą
                Project savedProject = projectRepository.save(project);

                // Atnaujiname užklausą
                request.setProject(savedProject);
                request.setConvertedToProject(true);
                contactRequestRepository.save(request);

                logger.info("Successfully created project {} for contactId {}", 
                    savedProject.getId(), contactId);

                return savedProject;
            });
    }

    
}