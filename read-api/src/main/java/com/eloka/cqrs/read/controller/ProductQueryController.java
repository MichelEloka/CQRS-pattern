package com.eloka.cqrs.read.controller;

import com.eloka.cqrs.read.dto.ProductViewResponse;
import com.eloka.cqrs.read.repository.ProductReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductQueryController.class);

    private final ProductReadRepository productReadRepository;
    private final String regionCode;

    public ProductQueryController(
            ProductReadRepository productReadRepository,
            @Value("${cqrs.region}") String regionCode
    ) {
        this.productReadRepository = productReadRepository;
        this.regionCode = regionCode;
    }

    @GetMapping
    public List<ProductViewResponse> findAll() {
        List<ProductViewResponse> products = productReadRepository.findAll();
        LOGGER.info("Read model in region {} returned {} products", regionCode, products.size());
        return products;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductViewResponse> findById(@PathVariable UUID productId) {
        Optional<ProductViewResponse> product = productReadRepository.findById(productId);
        product.ifPresentOrElse(
                response -> LOGGER.info("Read model in region {} returned product {}", regionCode, response.id()),
                () -> LOGGER.info("Read model in region {} did not find product {}", regionCode, productId)
        );
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
