package com.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(@NotNull Long userId, @NotBlank String currency) {}
