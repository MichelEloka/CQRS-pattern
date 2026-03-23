package com.eloka.cqrs.read.controller;

import com.eloka.cqrs.read.dto.ProductViewResponse;
import com.eloka.cqrs.read.repository.ProductReadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductQueryController {

    private final ProductReadRepository productReadRepository;

    public ProductQueryController(ProductReadRepository productReadRepository) {
        this.productReadRepository = productReadRepository;
    }

    @GetMapping
    public List<ProductViewResponse> findAll() {
        return productReadRepository.findAll();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductViewResponse> findById(@PathVariable UUID productId) {
        Optional<ProductViewResponse> product = productReadRepository.findById(productId);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

