package lt.ca.javau12.furnibay.controller;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.service.ContactRequestService;
import lt.ca.javau12.furnibay.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@CrossOrigin(
	    origins = "http://localhost:3001",
	    methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE }
	)
@RestController
@RequestMapping("/api/contact-requests")
public class ContactRequestController {

    @Autowired
    private ContactRequestService contactRequestService;

    @Autowired
    private ProjectService projectService;

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ContactRequestController() {
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ContactRequest> create(@RequestBody ContactRequest contactRequest) {
        return ResponseEntity.ok(contactRequestService.create(contactRequest));
    }


    @GetMapping
    public List<ContactRequest> getAll() {
        return contactRequestService.getAll();
    }

    @PostMapping("/convert/{contactId}")
    public ResponseEntity<?> convertContactToProject(@PathVariable Long contactId) {
        try {
            ContactRequest request = contactRequestService.getById(contactId);
            if (request == null) {
                return ResponseEntity.notFound().build();
            }

            Project project = projectService.convertContactToProject(contactId);
            if (project == null) {
                throw new RuntimeException("Conversion returned null project");
            }

            // Update the original request
            request.setConvertedToProject(true);
            ContactRequest updatedRequest = contactRequestService.save(request);
            
            if (!updatedRequest.isConvertedToProject()) {
                throw new RuntimeException("Failed to mark request as converted");
            }

            return ResponseEntity.ok(Map.of(
                "project", project,
                "contactRequest", updatedRequest
            ));
        } catch (Exception e) {
            System.err.println("Conversion error: " + e.getMessage());
            return ResponseEntity.status(500)
                .body("Conversion failed: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateRequest(
        @PathVariable Long id,
        @RequestBody Map<String, Object> updates) {
        
        try {
            ContactRequest existing = contactRequestService.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            // Handle date update
            if (updates.containsKey("deliveryDate")) {
                Object dateObj = updates.get("deliveryDate");
                try {
                    if (dateObj instanceof String) {
                        existing.setDeliveryDate(LocalDate.parse((String) dateObj, dateFormatter));
                    } else if (dateObj != null) {
                        existing.setDeliveryDate(objectMapper.convertValue(dateObj, LocalDate.class));
                    }
                } catch (DateTimeParseException e) {
                    return ResponseEntity.badRequest()
                        .body("Invalid date format. Expected yyyy-MM-dd");
                }
            }

            // Update other fields
            if (updates.containsKey("status")) {
                existing.setStatus((String) updates.get("status"));
            }
            if (updates.containsKey("orderPrice")) {
                existing.setOrderPrice((String) updates.get("orderPrice"));
            }
            if (updates.containsKey("notes")) {
                existing.setNotes((String) updates.get("notes"));
            }

            ContactRequest savedRequest = contactRequestService.save(existing);
            projectService.syncProjectWithContactRequest(savedRequest);

            // Log successful update
            System.out.println("Successfully updated request ID " + id + 
                " with deliveryDate: " + savedRequest.getDeliveryDate());

            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            System.err.println("Update error for request ID " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest()
                .body("Update failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/project")
    public ResponseEntity<Project> getProjectFromContactRequest(@PathVariable Long id) {
        return projectService.getProjectFromContactRequest(id)
                .map(project -> ResponseEntity.ok(project))
                .orElse(ResponseEntity.notFound().build());
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContactRequest(@PathVariable Long id) {
        contactRequestService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private LocalDate convertToLocalDate(Object dateObj) throws DateTimeParseException {
        if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj, dateFormatter);
        }
        return objectMapper.convertValue(dateObj, LocalDate.class);
    }
}