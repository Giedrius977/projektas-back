package lt.ca.javau12.furnibay.controller;

import lt.ca.javau12.furnibay.dto.ContactRequest;
import lt.ca.javau12.furnibay.service.ContactRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}