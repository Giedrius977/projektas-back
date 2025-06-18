package lt.ca.javau12.furnibay.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.UserRepository;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    // CRUD Operations
    @Transactional
    public Project createProject(Project project) {
        validateProject(project);
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id)
            .map(project -> {
                Hibernate.initialize(project.getUser());
                Hibernate.initialize(project.getContactRequest());
                return project;
            });
    }

    @Transactional
    public Project updateProject(Project project) {
        validateProject(project);
        return projectRepository.save(project);
    }

    @Transactional
    public boolean deleteProject(Long projectId) {
        return projectRepository.findById(projectId)
            .map(project -> {
                // Atsieti susijusią užklausą
                if (project.getContactRequest() != null) {
                    ContactRequest request = project.getContactRequest();
                    request.setProject(null);
                    contactRequestRepository.save(request);
                }
                
                projectRepository.delete(project);
                return true;
            })
            .orElse(false);
    }

    // Special Operations
    @Transactional
    public Project convertContactToProject(Long contactRequestId) {
        ContactRequest request = contactRequestRepository.findById(contactRequestId)
            .orElseThrow(() -> new EntityNotFoundException("ContactRequest not found"));

        if (request.getProject() != null) {
            return request.getProject();
        }

        Project project = new Project();
        project.setName(generateProjectName(request));
        project.setDescription(request.getMessage());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "New");
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());
        project.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDate.now());
        
        // Set relationships
        User user = findOrCreateUserFromRequest(request);
        project.setUser(user);
        project.setContactRequest(request);
        
        Project savedProject = projectRepository.save(project);
        
        // Update contact request
        request.setProject(savedProject);
        request.setConvertedToProject(true);
        contactRequestRepository.save(request);
        
        return savedProject;
    }

    // Query Methods
    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByUserId(userId);
    }

    public List<Project> getProjectsByUserEmail(String email) {
        return projectRepository.findByUserEmail(email);
    }

    public Optional<Project> getProjectByContactRequestId(Long contactRequestId) {
        return projectRepository.findByContactRequestId(contactRequestId)
            .map(project -> {
                Hibernate.initialize(project.getUser());
                return project;
            });
    }

    // Sync Methods
    @Transactional
    public void syncProjectWithContactRequest(ContactRequest request) {
        Project project = projectRepository.findByContactRequest(request)
            .orElseGet(() -> {
                Project newProject = createProjectFromRequest(request);
                request.setProject(newProject);
                return projectRepository.save(newProject);
            });

        // Sync fields
        if (project.getUser() == null && request.getUser() != null) {
            project.setUser(request.getUser());
        }
        
        project.setStatus(request.getStatus());
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());
        
        projectRepository.save(project);
    }
    
    @Transactional
    public void syncProjectStatusWithRequestStatus(ContactRequest request) {
        if (request.getProject() == null) {
            throw new IllegalStateException("ContactRequest has no associated project");
        }

        Project project = request.getProject();
        project.setStatus(request.getStatus());
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());

        projectRepository.save(project);
    }

    public Optional<Project> getProjectFromContactRequest(Long contactRequestId) {
        return contactRequestRepository.findById(contactRequestId)
            .map(ContactRequest::getProject);
    }


    // Helper Methods
    private Project createProjectFromRequest(ContactRequest request) {
        Project project = new Project();
        User user = findOrCreateUserFromRequest(request);
        
        project.setUser(user);
        project.setContactRequest(request);
        project.setName(generateProjectName(request));
        project.setDescription(request.getMessage());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "New");
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());
        project.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDate.now());
        
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

    private String generateProjectName(ContactRequest request) {
        return "Projektas iš " + request.getName() + " (" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ")";
    }

    private void validateProject(Project project) {
        if (project.getContactRequest() == null || project.getContactRequest().getId() == null) {
            throw new IllegalArgumentException("Project must be associated with a valid ContactRequest");
        }
        
        if (project.getUser() == null || project.getUser().getId() == null ||
            !userRepository.existsById(project.getUser().getId())) {
            throw new IllegalArgumentException("Project must be linked to an existing user");
        }
    }
}