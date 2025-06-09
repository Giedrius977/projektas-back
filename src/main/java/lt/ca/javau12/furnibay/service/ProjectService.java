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

    @Transactional
    public void syncProjectStatusWithRequestStatus(ContactRequest request) {
        if (request.getProject() == null) {
            throw new IllegalStateException("ContactRequest neturi susieto projekto");
        }

        Project project = request.getProject();
        project.setStatus(request.getStatus());
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

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByClient(String clientEmail) {
        return projectRepository.findByUserEmail(clientEmail);
    }

    @Transactional
    public void syncProjectWithContactRequest(ContactRequest request) {
        Project project = projectRepository.findByContactRequest(request)
                .orElseGet(() -> {
                    Project newProject = createProjectFromContactRequest(request);
                    request.setProject(newProject);
                    return projectRepository.save(newProject);
                });

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

        existing.setStatus(updatedProject.getStatus());
        existing.setDeliveryDate(updatedProject.getDeliveryDate());
        existing.setOrderPrice(updatedProject.getOrderPrice());
        existing.setNotes(updatedProject.getNotes());

        return projectRepository.save(existing);
    }

    @Transactional
    public Project convertContactToProject(Long contactId) {
        ContactRequest request = contactRequestRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact request not found"));

        if (request.getProject() != null) {
            return request.getProject();
        }

        request.setConvertedToProject(true);

        Project project = new Project();
        User user = findOrCreateUserFromRequest(request);

        project.setUser(user);
        project.setContactRequest(request);
        project.setName(generateProjectName(request));
        project.setDescription(request.getMessage());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "Vertinamas");
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());

        // Nustatome sukūrimo datą
        if (request.getCreatedAt() != null) {
          project.setCreatedAt(request.getCreatedAt());
        } else {
            project.setCreatedAt(LocalDateTime.now());
        }

        request.setProject(project);
        contactRequestRepository.save(request);
        return projectRepository.save(project);
    }

    @Transactional
    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Project createProjectFromContactRequest(ContactRequest request) {
        Project project = new Project();
        User user = findOrCreateUserFromRequest(request);

        project.setUser(user);
        project.setContactRequest(request);
        project.setName(generateProjectName(request));
        project.setDescription(request.getMessage());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "Vertinamas");
        project.setDeliveryDate(request.getDeliveryDate());
        project.setOrderPrice(request.getOrderPrice());
        project.setNotes(request.getNotes());

        if (request.getCreatedAt() != null) {
            project.setCreatedAt(request.getCreatedAt());
        } else {
            project.setCreatedAt(LocalDateTime.now());
        }

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

    private void validateProjectBeforeCreate(Project project) {
        if (project.getId() != null) {
            throw new IllegalArgumentException("Projektas neturi ID");
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
        return projectRepository.findByUserEmail(userEmail);
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getUser() != null) {
                        Hibernate.initialize(project.getUser());
                    }
                    return project;
                });
    }
}
