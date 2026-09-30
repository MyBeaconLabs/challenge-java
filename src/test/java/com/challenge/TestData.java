package com.challenge;

import com.challenge.dto.CreateUserRequest;
import com.challenge.entity.Wallet;
import com.challenge.service.UserService;
import com.challenge.service.WalletService;
import java.util.UUID;

public final class TestData {

    private TestData() {}

    public static Wallet newUserWallet(UserService userService, WalletService walletService, String currency) {
        String unique = UUID.randomUUID().toString();
        var user = userService.createUser(new CreateUserRequest("Test User", unique + "@example.com"));
        return walletService.createWallet(user.getId(), currency);
    }
}
