package lt.ca.javau12.furnibay.controller;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.Valid;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.service.ProjectService;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // Sujungtas GET metodas su JSON serializacija ir userEmail filtru
    @GetMapping
    public ResponseEntity<String> getAll(@RequestParam(required = false) String userEmail) {
        List<Project> projects = (userEmail != null)
            ? projectService.getProjectsByUserEmail(userEmail)
            : projectService.getAllProjects();

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            String json = mapper.writeValueAsString(projects);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Konvertuoja ContactRequest į Project
    @PostMapping("/convert-from-contact/{contactId}")
    public ResponseEntity<Project> convertContactToProject(@PathVariable Long contactId) {
        try {
            Project project = projectService.convertContactToProject(contactId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Grąžina vieną projektą pagal ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
            .map(project -> {
                Hibernate.initialize(project.getUser());
                Hibernate.initialize(project.getContactRequest());
                return ResponseEntity.ok(project);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // Ištrina projektą
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = projectService.deleteProject(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Atnaujina projektą
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
        @PathVariable Long id,
        @RequestBody @Valid Project updatedProject
    ) {
        try {
            if (!id.equals(updatedProject.getId())) {
                return ResponseEntity.badRequest().build();
            }

            Project updated = projectService.updateProject(updatedProject);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
