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
	    private final ProjectService projectService;
	    private final ObjectMapper objectMapper;
	    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    private static final Logger logger = LoggerFactory.getLogger(ContactRequestController.class);

 // Vienintelis konstruktorius
    @Autowired
    public ContactRequestController(ContactRequestService contactRequestService,
                                  ProjectService projectService) {
        this.contactRequestService = contactRequestService;
        this.projectService = projectService;
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    }
    @Autowired
    private ContactRequestRepository contactRequestRepository;
    

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ContactRequest> create(@RequestBody ContactRequest contactRequest) {
        ContactRequest created = contactRequestService.create(contactRequest);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<Project> convertToProject(@PathVariable Long id) {
        try {
            Project project = contactRequestService.convertContactToProject(id);
            return ResponseEntity.ok(project);
        } catch (EntityNotFoundException e) {
            logger.error("ContactRequest not found for conversion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Conversion failed for request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error during conversion: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
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
    
   
    
    @GetMapping("/{id}")
    public ResponseEntity<ContactRequest> getContactRequestById(@PathVariable Long id) {
        ContactRequest contactRequest = contactRequestService.findById(id);
        return ResponseEntity.ok(contactRequest);
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
    public ResponseEntity<?> deleteContactRequest(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "false") boolean forceDelete) {
        Optional<ContactRequest> optionalRequest = contactRequestRepository.findById(id);

        if (optionalRequest.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ContactRequest request = optionalRequest.get();

        if ((request.isConvertedToProject() || request.getProject() != null) && !forceDelete) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Negalima ištrinti užklausos, kuri jau paversta projektu (nebent forceDelete=true).");
        }

        // Jei yra susietas projektas, pašalinam jį
        if (request.getProject() != null) {
            projectService.deleteProjectById(request.getProject().getId());
        }

        contactRequestRepository.deleteById(id);
        return ResponseEntity.ok("Ištrinta užklausa (ir projektas, jei buvo).");
    }


    private LocalDate convertToLocalDate(Object dateObj) throws DateTimeParseException {
        if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj, dateFormatter);
        }
        return objectMapper.convertValue(dateObj, LocalDate.class);
    }
}