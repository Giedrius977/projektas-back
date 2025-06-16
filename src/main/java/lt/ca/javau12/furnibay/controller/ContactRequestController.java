package lt.ca.javau12.furnibay.controller;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.service.ContactRequestService;
import lt.ca.javau12.furnibay.service.ProjectService;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(
    origins = "http://localhost:3001",
    methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE }
)
@RestController
@RequestMapping("/api/contact-requests")
public class ContactRequestController {

    private final ContactRequestService contactRequestService;
    private final ContactRequestRepository contactRequestRepository;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger logger = LoggerFactory.getLogger(ContactRequestController.class);


    @Autowired
    public ContactRequestController(ContactRequestService contactRequestService,
                                    ContactRequestRepository contactRequestRepository,
                                    ProjectService projectService) {
        this.contactRequestService = contactRequestService;
        this.contactRequestRepository = contactRequestRepository;
        this.projectService = projectService;
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ContactRequest> create(@RequestBody ContactRequest contactRequest) {
        ContactRequest created = contactRequestService.create(contactRequest);
        return ResponseEntity.ok(created);
    }

	/*
	 * @PostMapping("/convert/{contactId}") public ResponseEntity<?>
	 * convertContactToProject(@PathVariable Long contactId) { try { ContactRequest
	 * request = contactRequestService.getById(contactId); if (request == null) {
	 * return ResponseEntity.notFound().build(); }
	 * 
	 * Project project = projectService.convertContactToProject(contactId); if
	 * (project == null) { return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body("Conversion failed: returned project is null"); }
	 * 
	 * request.setConvertedToProject(true); ContactRequest updatedRequest =
	 * contactRequestService.save(request);
	 * 
	 * if (!updatedRequest.isConvertedToProject()) { return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body("Conversion failed: could not mark as converted"); }
	 * 
	 * return ResponseEntity.ok(Map.of( "project", project, "contactRequest",
	 * updatedRequest )); } catch (Exception e) {
	 * System.err.println("Conversion error: " + e.getMessage()); return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body("Conversion failed: " + e.getMessage()); } }
	 */

    @PostMapping("/convert/{contactId}")
    public ResponseEntity<?> convertToProject(@PathVariable Long contactId) {
        logger.info("Starting conversion for contactId: {}", contactId);
        try {
            Project project = contactRequestService.convertContactToProject(contactId);
            logger.debug("Successfully converted contactId {} to projectId {}", contactId, project.getId());
            return ResponseEntity.ok(project);
        } catch (EntityNotFoundException e) {
            logger.warn("ContactRequest not found for id: {}", contactId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Conversion rejected for contactId {}: {}", contactId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error converting contactId {}: {}", contactId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Conversion failed: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ContactRequest>> getAll() {
        return ResponseEntity.ok(contactRequestService.getAll());
    }

    @GetMapping("/{id}/project")
    public ResponseEntity<Project> getProjectFromContactRequest(@PathVariable Long id) {
        Optional<Project> projectOpt = projectService.getProjectFromContactRequest(id);
        return projectOpt.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable Long id,
                                           @RequestBody Map<String, Object> updates) {
        try {
            ContactRequest existing = contactRequestService.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            // Date
            if (updates.containsKey("deliveryDate")) {
                try {
                    existing.setDeliveryDate(convertToLocalDate(updates.get("deliveryDate")));
                } catch (DateTimeParseException e) {
                    return ResponseEntity.badRequest()
                            .body("Invalid date format. Expected yyyy-MM-dd");
                }
            }

            // Other fields
            if (updates.containsKey("status")) {
                existing.setStatus(objectMapper.convertValue(updates.get("status"), String.class));
            }

            if (updates.containsKey("orderPrice")) {
                existing.setOrderPrice(objectMapper.convertValue(updates.get("orderPrice"), String.class));
            }

            if (updates.containsKey("notes")) {
                existing.setNotes(objectMapper.convertValue(updates.get("notes"), String.class));
            }

            ContactRequest savedRequest = contactRequestService.save(existing);
            projectService.syncProjectWithContactRequest(savedRequest);

            System.out.println("Successfully updated request ID " + id +
                               " with deliveryDate: " + savedRequest.getDeliveryDate());

            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            System.err.println("Update error for request ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteContactRequest(@PathVariable Long id) {
        try {
            boolean deleted = contactRequestService.deleteById(id);
            if (deleted) {
                contactRequestRepository.flush();
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Delete error for request ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private LocalDate convertToLocalDate(Object dateObj) throws DateTimeParseException {
        if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj, dateFormatter);
        }
        return objectMapper.convertValue(dateObj, LocalDate.class);
    }
}