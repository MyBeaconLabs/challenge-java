package com.challenge.service;

import com.challenge.dto.CreateUserRequest;
import com.challenge.entity.User;
import com.challenge.exception.ConflictException;
import com.challenge.exception.NotFoundException;
import com.challenge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("A user with this email already exists");
        }
        return userRepository.save(new User(request.name(), request.email()));
    }

    @Transactional
    public User updateUser(Long id, CreateUserRequest request) {
        User user = getUser(id);
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("A user with this email already exists");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }
}
