package lt.ca.javau12.furnibay.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import lt.ca.javau12.furnibay.ContactRequest;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.Step;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.UserRepository;

@Service
public class ContactRequestService {

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectService projectService;

    // Sukuria naują kontaktinę užklausą
    public ContactRequest create(ContactRequest request) {
        request.setCreatedAt(LocalDateTime.now());
        return contactRequestRepository.save(request);
    }

    // Gauti visus ContactRequest
    public List<ContactRequest> getAll() {
        return contactRequestRepository.findAll();
    }

    // Gauti pagal ID
    public ContactRequest getById(Long id) {
        return contactRequestRepository.findById(id).orElse(null);
    }

    // Išsaugoti atnaujintą ContactRequest
    public ContactRequest save(ContactRequest request) {
        return contactRequestRepository.save(request);
    }

    // Ištrinti pagal ID
    public boolean deleteById(Long id) {
        if (contactRequestRepository.existsById(id)) {
            contactRequestRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Atnaujinti statusą + sinchronizuoti su projektu
    @Transactional
    public ContactRequest updateStatus(Long contactRequestId, String newStatus) {
        ContactRequest request = contactRequestRepository.findById(contactRequestId)
            .orElseThrow(() -> new IllegalArgumentException("ContactRequest nerastas su ID: " + contactRequestId));

        request.setStatus(newStatus);
        contactRequestRepository.save(request);

        projectService.syncProjectStatusWithRequestStatus(request);

        return request;
    }

    // Gauti projektą iš ContactRequest
    public Project getProjectFromContactRequest(Long contactRequestId) {
        ContactRequest request = contactRequestRepository.findById(contactRequestId)
            .orElseThrow(() -> new EntityNotFoundException("ContactRequest nerastas"));

        if (request.getProject() == null) {
            throw new EntityNotFoundException("Šis ContactRequest neturi susieto projekto");
        }

        return request.getProject();
    }

    // Konvertuoti ContactRequest į Project
    @Transactional
    public Project convertContactToProject(Long contactId) {
        ContactRequest request = contactRequestRepository.findById(contactId)
            .orElseThrow(() -> new EntityNotFoundException("ContactRequest nerastas"));

        if (request.getProject() != null) {
            Project existingProject = request.getProject();
            existingProject.setStatus(request.getStatus());
            existingProject.setNotes((existingProject.getNotes() != null ? existingProject.getNotes() + "\n" : "")
                    + "Papildyta iš admino panelės.");
            return projectRepository.save(existingProject);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Naudotojas nerastas: " + request.getEmail()));

        return createNewProjectFromRequest(request, user);
    }

    // Pagal ContactRequest sukurti naują Project
    private Project createNewProjectFromRequest(ContactRequest request, User user) {
        Project project = new Project();
        project.setUser(user);
        project.setName("Projektas pagal kontaktinę užklausą");
        project.setDescription(request.getMessage());
        project.setCreatedAt(LocalDateTime.now());
        project.setContactRequest(request);
        project.setStatus(request.getStatus());
        project.setNotes("Užklausa iš kontaktinės formos.\nFailas: " +
                (request.getFile() != null ? "Pridėtas" : "Nepateiktas"));

        List<Step> steps = Arrays.asList(
                new Step("Užklausa gauta", true, project),
                new Step("Vertinimas", false, project),
                new Step("Projektavimas", false, project),
                new Step("Gamyba", false, project),
                new Step("Pristatymas", false, project)
        );

        project.setSteps(steps);
        request.setProject(project);

        projectRepository.save(project);
        contactRequestRepository.save(request);

        return project;
    }
}
