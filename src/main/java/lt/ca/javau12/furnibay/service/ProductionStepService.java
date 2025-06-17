package lt.ca.javau12.furnibay.service;

import java.util.List;
import jakarta.transaction.Transactional;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.Step;
import lt.ca.javau12.furnibay.Priority; // Įsitikinkite, kad tai jūsų enum
import lt.ca.javau12.furnibay.repository.StepRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional // Galima dėti ir čia klasės lygyje
public class ProductionStepService {

    private final StepRepository stepRepository;
    
    // @Autowired - nebūtina, jei naudojate Spring 4.3+
    public ProductionStepService(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }
    
    public List<Step> initializeProductionSteps(Project project) {
        List<Step> steps = List.of(
            createStep("Žaliavų paruošimas", project, "Gamybos skyrius", Priority.HIGH, 1),
            createStep("Komponentų surinkimas", project, "Surinkimo skyrius", Priority.MEDIUM, 2),
            createStep("Kokybės kontrolė", project, "Kokybės skyrius", Priority.HIGH, 3)
        );
        return stepRepository.saveAll(steps);
    }
    
    private Step createStep(String title, Project project, String department, 
                          Priority priority, Integer order) {
        Step step = new Step();
        step.setTitle(title);
        step.setProject(project);
        step.setDepartment(department);
        step.setPriority(priority);
        step.setOrder(order);
        step.setDone(false);
        return step;
    }
}