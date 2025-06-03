package lt.ca.javau12.furnibay.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.Step;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.dto.ContactRequest;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.UserRepository;

@Service
public class ContactRequestService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    // Metodas, kuris apdoroja kontaktinę užklausą ir sukuria projektą
    public void handleContactRequest(ContactRequest request) {
        // 1. Surasti naudotoją pagal el. paštą
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Naudotojas su el. paštu nerastas: " + request.getEmail()));

        // 2. Sukurti naują projektą pagal užklausą
        Project project = new Project();
        project.setUser(user);
        project.setName("Projektas pagal kontaktinę užklausą");
        project.setDescription(request.getMessage());
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("Vertinamas");
        project.setNotes("Užklausa iš kontaktinės formos.\nFailas: " + (request.getFile() != null ? "Pridėtas" : "Nepateiktas"));

        // 3. Sukurti pradinius etapus projekto eigai
        Step step1 = new Step("Užklausa gauta", true, project);
        Step step2 = new Step("Vertinimas", false, project);
        Step step3 = new Step("Projektavimas", false, project);
        Step step4 = new Step("Gamyba", false, project);
        Step step5 = new Step("Pristatymas", false, project);

        project.setSteps(Arrays.asList(step1, step2, step3, step4, step5));

        // 4. Išsaugoti projektą į DB
        projectRepository.save(project);
    }
    public List<ContactRequest> getAll() {
        return contactRequestRepository.findAll();
    }
    
    // Metodas išsaugoti ContactRequest (jei reikia)
    public ContactRequest create(ContactRequest request) {
        return contactRequestRepository.save(request);
    }
    public boolean deleteById(Long id) {
        if (contactRequestRepository.existsById(id)) {
            contactRequestRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
