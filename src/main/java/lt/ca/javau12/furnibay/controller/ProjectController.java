package lt.ca.javau12.furnibay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // Gauti visus projektus
    @GetMapping
    public List<Project> getAll() {
        return projectService.getAllProjects();
    }

 // ProjectController.java

    @PostMapping("/convert-from-contact/{contactId}")
    public ResponseEntity<Project> convertContactToProject(@PathVariable Long contactId) {
        try {
            Project project = projectService.convertContactToProject(contactId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    // Gauti projektą pagal ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ištrinti projektą
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = projectService.deleteProject(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Atnaujinti projektą
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody @Valid Project updatedProject) {
        if (!id.equals(updatedProject.getId())) {
            return ResponseEntity.badRequest().build(); // ID nesutampa
        }

        try {
            Project updated = projectService.updateProject(updatedProject);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
