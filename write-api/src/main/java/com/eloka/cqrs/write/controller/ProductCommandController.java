package com.eloka.cqrs.write.controller;

import com.eloka.cqrs.common.events.ProductSnapshot;
import com.eloka.cqrs.write.dto.PatchProductRequest;
import com.eloka.cqrs.write.dto.UpsertProductRequest;
import com.eloka.cqrs.write.service.ProductCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/products")
public class ProductCommandController {

    private final ProductCommandService productCommandService;

    public ProductCommandController(ProductCommandService productCommandService) {
        this.productCommandService = productCommandService;
    }

    @PostMapping
    public ResponseEntity<ProductSnapshot> create(@Valid @RequestBody UpsertProductRequest request) {
        ProductSnapshot createdProduct = productCommandService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductSnapshot> replace(
            @PathVariable UUID productId,
            @Valid @RequestBody UpsertProductRequest request
    ) {
        Optional<ProductSnapshot> updatedProduct = productCommandService.replace(productId, request);
        return updatedProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductSnapshot> patch(
            @PathVariable UUID productId,
            @Valid @RequestBody PatchProductRequest request
    ) {
        Optional<ProductSnapshot> updatedProduct = productCommandService.patch(productId, request);
        return updatedProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

