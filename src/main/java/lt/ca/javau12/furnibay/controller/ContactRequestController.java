package lt.ca.javau12.furnibay.controller;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.service.ContactRequestService;
import lt.ca.javau12.furnibay.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/contact-requests")
public class ContactRequestController {

    @Autowired
    private ContactRequestService contactRequestService;

    @Autowired
    private ProjectService projectService;

    // Sukurti naują ContactRequest
    @PostMapping
    public ResponseEntity<ContactRequest> create(@RequestBody ContactRequest request) {
        ContactRequest savedRequest = contactRequestService.create(request);
        return ResponseEntity.ok(savedRequest);
    }

    // Gauti visus ContactRequest
    @GetMapping
    public List<ContactRequest> getAll() {
        return contactRequestService.getAll();
    }

    // Konvertuoti ContactRequest į Project
    @PostMapping("/convert/{contactId}")
    public ResponseEntity<?> convertContactToProject(@PathVariable Long contactId) {
        try {
            Project project = projectService.convertContactToProject(contactId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Nepavyko konvertuoti į projektą: " + e.getMessage());
        }
    }

    // PATCH – dalinis ContactRequest atnaujinimas
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateRequest(
        @PathVariable Long id,
        @RequestBody String rawJson
    ) {
        try {
            // Validuojame JSON
            new ObjectMapper().readTree(rawJson);

            ContactRequest partialRequest = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(rawJson, ContactRequest.class);

            ContactRequest existing = contactRequestService.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            // Atnaujiname reikšmes tik jei pateiktos
            if (partialRequest.getStatus() != null) {
                existing.setStatus(partialRequest.getStatus());
            }
            if (partialRequest.getDeliveryDate() != null) {
                existing.setDeliveryDate(partialRequest.getDeliveryDate());
            }
            if (partialRequest.getOrderPrice() != null) {
                existing.setOrderPrice(partialRequest.getOrderPrice());
            }
            if (partialRequest.getNotes() != null) {
                existing.setNotes(partialRequest.getNotes());
            }

            ContactRequest savedRequest = contactRequestService.save(existing);

            // Sinchronizuoti susijusį projektą
            projectService.syncProjectWithContactRequest(savedRequest);

            return ResponseEntity.ok(savedRequest);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Netinkamas JSON formatas: " + e.getMessage());
        }
    }

    // Gauti Project iš ContactRequest
    @GetMapping("/{id}/project")
    public ResponseEntity<Project> getProjectFromContactRequest(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectFromContactRequest(id);
            return ResponseEntity.ok(project);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
