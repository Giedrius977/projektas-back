package lt.ca.javau12.furnibay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.service.ProjectService;
import lt.ca.javau12.furnibay.service.UserService;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid User updatedUser) {
        return userService.getUserById(id)
            .map(existing -> {
                existing.setName(updatedUser.getName());
                existing.setEmail(updatedUser.getEmail());
                existing.setPhone(updatedUser.getPhone());             // pridėta
                existing.setDescription(updatedUser.getDescription()); // pridėta
                return ResponseEntity.ok(userService.createUser(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/by-username/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping("/{userId}/projects")
    public ResponseEntity<List<Project>> getUserProjects(@PathVariable Long userId) {
        List<Project> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projects);
    }
}
    