package lt.ca.javau12.furnibay.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lt.ca.javau12.furnibay.Project;
import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.repository.UserRepository;
import lt.ca.javau12.furnibay.repository.ProjectRepository;
import lt.ca.javau12.furnibay.repository.ContactRequestRepository;

@Service
public class UserService {
	
	
 @Autowired
 private UserRepository userRepository;
 
 @Autowired 
 private ProjectRepository projectRepository;
 
 @Autowired
 private ContactRequestRepository contactRequestRepository;

 public User createUser(User user) {
     if (userRepository.findByEmail(user.getEmail()).isPresent()) {
         throw new IllegalArgumentException("El. paštas jau naudojamas");
     }
     return userRepository.save(user);
 }
 

 @Transactional
 public boolean safeDeleteUser(Long userId) {
     return userRepository.findById(userId)
         .map(user -> {
             boolean hasProjects = projectRepository.existsByUser(user);
             boolean hasRequests = contactRequestRepository.existsByUser(user);
             
             if (!hasProjects && !hasRequests) {
                 userRepository.delete(user);
                 return true;
             }
             return false;
         })
         .orElse(false);
 }
 
 
 public List<User> getAllUsers() {
     return userRepository.findAll();
 	}

 public Optional<User> getUserById(Long id) {
     return userRepository.findById(id);
 	}

 public boolean deleteUser(Long id) {
	    if (userRepository.existsById(id)) {
	        userRepository.deleteById(id);
	        return true;
	    }
	    return false;
	}
 
 public Optional<User> getUserByUsername(String username) {
	    List<User> users = userRepository.findAllByName(username);
	    return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
	}

 public List<Project> getUserProjects(Long userId) {
     return userRepository.findProjectsByUserId(userId);
 	}
}

 
 