package com.challenge.controller;

import com.challenge.dto.CreateUserRequest;
import com.challenge.dto.UserResponse;
import com.challenge.dto.WalletResponse;
import com.challenge.service.UserService;
import com.challenge.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final WalletService walletService;

    public UserController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(userService.getUser(id));
    }

    @GetMapping("/{id}/wallets")
    public List<WalletResponse> getWallets(@PathVariable Long id) {
        userService.getUser(id);
        return walletService.getWalletsForUser(id).stream().map(WalletResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.updateUser(id, request));
    }
}
