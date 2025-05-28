package lt.ca.javau12.furnibay.service;
import lt.ca.javau12.furnibay.Step;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.dto.ContactRequest;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.UserRepository;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // Gauti visus projektus
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Sukurti projektą su validacija
    public Project createProject(Project project) {
        if (project.getId() != null) {
            throw new IllegalArgumentException("New project shouldn't have ID");
        }

        if (project.getUser() == null || project.getUser().getId() == null ||
            !userRepository.existsById(project.getUser().getId())) {
            throw new IllegalArgumentException("Project must be linked to an existing user.");
        }

        return projectRepository.save(project);
    }

    // Atnaujinti projektą su validacija
    public Project updateProject(Project project) {
        if (project.getId() == null || !projectRepository.existsById(project.getId())) {
            throw new EntityNotFoundException("Project not found");
        }

        if (project.getUser() == null || project.getUser().getId() == null ||
            !userRepository.existsById(project.getUser().getId())) {
            throw new IllegalArgumentException("Project must be linked to an existing user.");
        }

        return projectRepository.save(project);
    }
    @Autowired
    private ContactRequestRepository contactRequestRepository;

    public Project convertContactToProject(Long contactId) {
        ContactRequest request = contactRequestRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("ContactRequest not found"));

        return createProjectFromContactRequest(request);
    }

    
    // Gauti projektą pagal ID
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    // Ištrinti projektą
    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ Naujas metodas: sukuria projektą iš ContactRequest
    public Project createProjectFromContactRequest(ContactRequest request) {
        Project project = new Project();
        request.setConvertedToProject(true);
        contactRequestRepository.save(request);


        // Susiejame arba sukuriame vartotoją
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> createUserFromContact(request));
        project.setUser(user);

        project.setName(request.getName());
        project.setDescription(request.getMessage());
        project.setStatus("Vertinamas");
        project.setCreatedAt(parseCreatedAt(request.getCreatedAt()));

        // Žingsniai
        List<Step> steps = new ArrayList<>();
        steps.add(createStep("Užklausa gauta", true));
        steps.add(createStep("Vertinimas", false));
        steps.add(createStep("Projektavimas", false));
        steps.add(createStep("Gamyba", false));
        steps.add(createStep("Pristatymas", false));

        for (Step step : steps) {
            step.setProject(project);
        }

        project.setSteps(steps);
        return projectRepository.save(project);
    }

    private User createUserFromContact(ContactRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return userRepository.save(user);
    }

    private Step createStep(String title, boolean done) {
        return new Step(title, done, null); // null perduodamas kaip project, jį gali nustatyti vėliau
    }


    private LocalDateTime parseCreatedAt(String createdAt) {
        try {
            return LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
