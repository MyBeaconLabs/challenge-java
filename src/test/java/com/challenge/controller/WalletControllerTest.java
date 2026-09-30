package com.challenge.controller;

import com.challenge.IntegrationTest;
import com.challenge.TestData;
import com.challenge.entity.Wallet;
import com.challenge.service.UserService;
import com.challenge.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WalletControllerTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired WalletService walletService;

    @Test
    void depositIsIdempotentOnKey() throws Exception {
        Wallet wallet = TestData.newUserWallet(userService, walletService, "USD");

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/wallets/{id}/deposits", wallet.getId())
                    .header("Idempotency-Key", "dep-" + wallet.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\": 12.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceMinor").value(1250))
                .andExpect(jsonPath("$.balance").value(12.50));
        }
    }

    @Test
    void depositRequiresIdempotencyKey() throws Exception {
        Wallet wallet = TestData.newUserWallet(userService, walletService, "USD");

        mockMvc.perform(post("/api/wallets/{id}/deposits", wallet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 5}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void depositRejectsSubCentAmounts() throws Exception {
        Wallet wallet = TestData.newUserWallet(userService, walletService, "USD");

        mockMvc.perform(post("/api/wallets/{id}/deposits", wallet.getId())
                .header("Idempotency-Key", "k1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 10.005}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unknownWalletIs404() throws Exception {
        mockMvc.perform(get("/api/wallets/{id}", 999_999))
            .andExpect(status().isNotFound());
    }
}
