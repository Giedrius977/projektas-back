package lt.ca.javau12.furnibay.controller;

import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.service.ProjectService;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // 1. Gauti visus projektus arba filtruoti pagal vartotojo el. paštą
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(
        @RequestParam(required = false) String userEmail) {
        
        List<Project> projects = (userEmail != null)
            ? projectService.getProjectsByUserEmail(userEmail)
            : projectService.getAllProjects();
        
        return ResponseEntity.ok(projects);
    }

    // 2. Sukurti naują projektą
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        try {
            Project createdProject = projectService.createProject(project);
            return ResponseEntity.ok(createdProject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 3. Gauti projektą pagal ID
    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long projectId) {
        return projectService.getProjectById(projectId)
            .map(project -> {
                Hibernate.initialize(project.getUser());
                Hibernate.initialize(project.getContactRequest());
                return ResponseEntity.ok(project);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // 4. Atnaujinti projektą
    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(
        @PathVariable Long projectId,
        @RequestBody Project updatedProject) {
        
        try {
            if (!projectId.equals(updatedProject.getId())) {
                return ResponseEntity.badRequest().build();
            }

            Project updated = projectService.updateProject(updatedProject);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Ištrinti projektą
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        boolean deleted = projectService.deleteProject(projectId);
        return deleted 
            ? ResponseEntity.noContent().build() 
            : ResponseEntity.notFound().build();
    }

    // 6. Konvertuoti kontaktinę užklausą į projektą
    @PostMapping("/convert/{contactRequestId}")
    public ResponseEntity<Project> convertContactToProject(
        @PathVariable Long contactRequestId) {
        
        try {
            Project project = projectService.convertContactToProject(contactRequestId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 7. Gauti projektus pagal vartotojo ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Project>> getProjectsByUserId(
        @PathVariable Long userId) {
        
        List<Project> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projects);
    }
}