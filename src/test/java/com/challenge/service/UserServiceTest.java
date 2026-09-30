package com.challenge.service;

import com.challenge.dto.CreateUserRequest;
import com.challenge.entity.User;
import com.challenge.exception.ConflictException;
import com.challenge.exception.NotFoundException;
import com.challenge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final CreateUserRequest request = new CreateUserRequest("John Doe", "john.doe@example.com");

    @Test
    void createUser_savesNewUser() {
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(request);

        assertThat(created.getEmail()).isEqualTo(request.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUser_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L)).isInstanceOf(NotFoundException.class);
    }
}
