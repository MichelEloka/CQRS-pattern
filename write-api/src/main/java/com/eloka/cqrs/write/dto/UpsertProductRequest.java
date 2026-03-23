package com.eloka.cqrs.write.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpsertProductRequest(
        @NotBlank(message = "name est obligatoire.")
        String name,
        String description,
        @NotNull(message = "price est obligatoire.")
        @DecimalMin(value = "0.0", inclusive = true, message = "price doit etre positif.")
        BigDecimal price,
        @NotNull(message = "stock est obligatoire.")
        @PositiveOrZero(message = "stock doit etre positif ou nul.")
        Integer stock,
        String status
) {
}

