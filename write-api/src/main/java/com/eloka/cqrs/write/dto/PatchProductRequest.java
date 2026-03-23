package com.eloka.cqrs.write.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PatchProductRequest(
        String name,
        String description,
        @DecimalMin(value = "0.0", inclusive = true, message = "price doit etre positif.")
        BigDecimal price,
        @PositiveOrZero(message = "stock doit etre positif ou nul.")
        Integer stock,
        String status
) {
    public boolean isEmpty() {
        return name == null && description == null && price == null && stock == null && status == null;
    }
}

