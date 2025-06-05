package lt.ca.javau12.furnibay.controller;

import lt.ca.javau12.furnibay.dto.ContactRequest;
import lt.ca.javau12.furnibay.service.ContactRequestService;
import lt.ca.javau12.furnibay.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/contact-requests")
public class ContactRequestController {

    @Autowired
    private ContactRequestService contactRequestService;

    @PostMapping
    public ResponseEntity<ContactRequest> create(@RequestBody ContactRequest request) {
        ContactRequest savedRequest = contactRequestService.create(request);
        return ResponseEntity.ok(savedRequest);
    }
    
    @GetMapping
    public List<ContactRequest> getAll() {
        return contactRequestService.getAll();
    }
    @Autowired
    private ProjectService projectService;

    @PostMapping("/convert/{contactId}")
    public ResponseEntity<?> convertContactToProject(@PathVariable Long contactId) {
        try {
            return ResponseEntity.ok(projectService.convertContactToProject(contactId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Nepavyko konvertuoti į projektą: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody ContactRequest partialRequest) {
        try {
            ContactRequest existing = contactRequestService.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            if (partialRequest.getStatus() != null) {
                existing.setStatus(partialRequest.getStatus());
                contactRequestService.save(existing); // arba update jei toks metodas yra
            }

            return ResponseEntity.ok(existing);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Klaida saugant būseną: " + e.getMessage());
        }
    }

    
}