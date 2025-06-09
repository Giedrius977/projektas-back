package lt.ca.javau12.furnibay.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.Step;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.UserRepository;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public void syncProjectStatusWithRequestStatus(ContactRequest request) {
        if (request.getProject() == null) {
            throw new IllegalStateException("ContactRequest neturi susieto projekto");
        }

        Project project = request.getProject();

        // Sinchronizuojame projekto statusą su užklausos statusu
        project.setStatus(request.getStatus());

        // Papildomai sinchronizuojame kitą informaciją, jei reikia
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());

        projectRepository.save(project);
    }

    public Project getProjectFromContactRequest(Long contactRequestId) {
        ContactRequest request = contactRequestRepository.findById(contactRequestId)
            .orElseThrow(() -> new EntityNotFoundException("ContactRequest nerastas"));

        if (request.getProject() == null) {
            throw new EntityNotFoundException("Šis ContactRequest neturi susieto projekto");
        }

        return request.getProject();
    }
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    // Gauti visus projektus
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByClient(String clientEmail) {
        return projectRepository.findByUserEmail(clientEmail);
    }

    @Transactional
    public void syncProjectWithContactRequest(ContactRequest request) {
        // 1. Rasti esamą projektą
        Project project = projectRepository.findByContactRequest(request)
            .orElseGet(() -> {
                // 2. Jei projekto nėra - sukurti naują
                Project newProject = createProjectFromContactRequest(request);
                request.setProject(newProject);
                return projectRepository.save(newProject);
            });
        
        // 3. Atnaujinti projekto duomenis
        project.setStatus(request.getStatus());
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());
        
        projectRepository.save(project);
    
    }

    @Transactional
    public Project createProject(Project project) {
        validateProjectBeforeCreate(project);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Project updatedProject) {
        Project existing = projectRepository.findById(updatedProject.getId())
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        // Atnaujinti tik keičiamus laukus
        existing.setStatus(updatedProject.getStatus());
        existing.setDeliveryDate(updatedProject.getDeliveryDate());
        existing.setOrderPrice(updatedProject.getOrderPrice());
        existing.setNotes(updatedProject.getNotes());
        
        return projectRepository.save(existing);
    }

    @Transactional
    public Project convertContactToProject(Long contactId) {
        ContactRequest request = contactRequestRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("ContactRequest not found"));

        // Tikriname ar jau yra projektas
        if (request.getProject() != null) {
            return request.getProject(); // Grąžiname jau esantį projektą
        }

        request.setConvertedToProject(true);
        Project project = createProjectFromContactRequest(request);
        request.setProject(project);
        
        contactRequestRepository.save(request);
        return projectRepository.save(project);
    }

    // Ištrinti projektą
    @Transactional
    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Pagalbiniai metodai

    private Project createProjectFromContactRequest(ContactRequest request) {
        Project project = new Project();
        User user = findOrCreateUserFromRequest(request);
        
        project.setUser(user);
        project.setContactRequest(request); // Naudojant naują ryšį
        project.setName(generateProjectName(request));
        project.setDescription(request.getMessage());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "Vertinamas");
        
        // Perkeliame svarbius laukus
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());
        
        return project;
    }

    private User findOrCreateUserFromRequest(ContactRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setName(request.getName());
                newUser.setEmail(request.getEmail());
                newUser.setPhone(request.getPhone());
                return userRepository.save(newUser);
            });
    }

    private List<Step> createDefaultSteps(Project project) {
        List<Step> steps = new ArrayList<>();
        steps.add(createStep("Užklausa gauta", true, project));
        steps.add(createStep("Vertinimas", false, project));
        steps.add(createStep("Projektavimas", false, project));
        steps.add(createStep("Gamyba", false, project));
        steps.add(createStep("Pristatymas", false, project));
        return steps;
    }

    private Step createStep(String title, boolean done, Project project) {
        Step step = new Step();
        step.setTitle(title);
        step.setDone(done);
        step.setProject(project);
        return step;
    }

    private void updateProjectFromRequest(ContactRequest request, Project project) {
        if (request.getStatus() != null && !request.getStatus().equals(project.getStatus())) {
            project.setStatus(request.getStatus());
        }
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());
    }

    private void syncAdditionalFieldsFromRequest(Project project, ContactRequest request) {
        if (project.getDeliveryDate() == null) {
            project.setDeliveryDate(request.getDeliveryDate());
        }
        if (project.getOrderPrice() == null) {
            project.setOrderPrice(request.getOrderPrice());
        }
        if (project.getNotes() == null || project.getNotes().isEmpty()) {
            project.setNotes(request.getNotes());
        }
    }

    private String generateProjectName(ContactRequest request) {
        return "Projektas iš " + request.getName() + " (" + 
               LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ")";
    }

    private LocalDateTime parseCreatedAt(String createdAt) {
        try {
            return LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private void validateProjectBeforeCreate(Project project) {
        if (project.getId() != null) {
            throw new IllegalArgumentException("Projektas neturi ID");
        }
        validateUserAssociation(project);
    }

    private void validateProjectBeforeUpdate(Project project) {
        if (project.getId() == null || !projectRepository.existsById(project.getId())) {
            throw new EntityNotFoundException("Projektas nerastas");
        }
        validateUserAssociation(project);
    }

    private void validateUserAssociation(Project project) {
        if (project.getUser() == null || project.getUser().getId() == null ||
            !userRepository.existsById(project.getUser().getId())) {
            throw new IllegalArgumentException("Project must be linked to an existing user");
        }
    }

    public List<Project> getProjectsByUserEmail(String userEmail) {
        // Variantas 1: Naudojant Query repository
        return projectRepository.findByUserEmail(userEmail);
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    // Jei reikia papildomai užkrauti susijusius duomenis
                    if (project.getUser() != null) {
                        // Užkraunam user objektą, jei reikia
                        Hibernate.initialize(project.getUser());
                    }
                    return project;
                });
    }
}