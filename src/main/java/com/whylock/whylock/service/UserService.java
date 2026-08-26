package com.whylock.whylock.service;

import com.whylock.whylock.model.User;
import com.whylock.whylock.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.transaction.annotation.Transactional
    public void register(String username, String rawPassword, String role) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("User already exists");
            }
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(rawPassword));
            if (role == null) {
                u.setRole(User.Role.USER);
            } else {
                try {
                    u.setRole(User.Role.valueOf(role.toUpperCase()));
                } catch (Exception e) {
                    u.setRole(User.Role.USER);
                }
            }
            userRepository.save(u);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to register user", ex);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }
}
