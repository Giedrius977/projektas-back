package lt.ca.javau12.furnibay.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lt.ca.javau12.furnibay.User;
import lt.ca.javau12.furnibay.repository.UserRepository;

//UserService.java
@Service
public class UserService {
 @Autowired
 private UserRepository userRepository;

 public List<User> getAllUsers() {
     return userRepository.findAll();
 }

 public User createUser(User user) {
     return userRepository.save(user);
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

}
