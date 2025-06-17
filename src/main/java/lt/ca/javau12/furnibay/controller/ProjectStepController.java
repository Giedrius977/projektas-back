package lt.ca.javau12.furnibay.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.Step;
import lt.ca.javau12.furnibay.service.ProductionStepService;
import lt.ca.javau12.furnibay.service.ProjectService;

@RestController
@RequestMapping("/api/projects/{projectId}/steps")
public class ProjectStepController {
    
    private final ProductionStepService productionStepService;
    private final ProjectService projectService;
    
    // Rankiniu būdu sukurtas konstruktorius
    public ProjectStepController(ProductionStepService productionStepService, 
                               ProjectService projectService) {
        this.productionStepService = productionStepService;
        this.projectService = projectService;
    }
    @PostMapping("/initialize-production")
    public ResponseEntity<List<Step>> initializeProductionSteps(@PathVariable Long projectId) {
        Project project = projectService.getProjectById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Projektas nerastas"));
        
        List<Step> createdSteps = productionStepService.initializeProductionSteps(project);
        return ResponseEntity.ok(createdSteps);
    }
}