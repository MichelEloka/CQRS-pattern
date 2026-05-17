package com.eloka.cqrs.write.repository;

import com.eloka.cqrs.common.domain.ProductStatus;
import com.eloka.cqrs.common.events.ProductSnapshot;
import com.eloka.cqrs.write.dto.PatchProductRequest;
import com.eloka.cqrs.write.dto.UpsertProductRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductWriteRepository {

    private static final RowMapper<ProductSnapshot> PRODUCT_ROW_MAPPER = (rs, rowNum) -> new ProductSnapshot(
            rs.getObject("id", UUID.class),
            rs.getString("region"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"),
            ProductStatus.valueOf(rs.getString("status")),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductWriteRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ProductSnapshot insert(UUID productId, String region, UpsertProductRequest request) {
        MapSqlParameterSource parameters = baseParameters(productId)
                .addValue("region", region)
                .addValue("name", normalizeRequiredText(request.name(), "name"))
                .addValue("description", normalizeOptionalText(request.description()))
                .addValue("price", normalizePrice(request.price()))
                .addValue("stock", request.stock())
                .addValue("status", ProductStatus.fromNullable(request.status()).name());

        return jdbcTemplate.queryForObject("""
                INSERT INTO products (id, region, name, description, price, stock, status)
                VALUES (:id, :region, :name, :description, :price, :stock, :status)
                RETURNING id, region, name, description, price, stock, status, created_at, updated_at
                """, parameters, PRODUCT_ROW_MAPPER);
    }

    public Optional<ProductSnapshot> replace(UUID productId, String region, UpsertProductRequest request) {
        MapSqlParameterSource parameters = baseParameters(productId)
                .addValue("region", region)
                .addValue("name", normalizeRequiredText(request.name(), "name"))
                .addValue("description", normalizeOptionalText(request.description()))
                .addValue("price", normalizePrice(request.price()))
                .addValue("stock", request.stock())
                .addValue("status", ProductStatus.fromNullable(request.status()).name());

        return jdbcTemplate.query("""
                UPDATE products
                SET name = :name,
                    description = :description,
                    price = :price,
                    stock = :stock,
                    status = :status
                WHERE id = :id
                  AND region = :region
                RETURNING id, region, name, description, price, stock, status, created_at, updated_at
                """, parameters, PRODUCT_ROW_MAPPER).stream().findFirst();
    }

    public Optional<ProductSnapshot> patch(UUID productId, String region, PatchProductRequest request) {
        MapSqlParameterSource parameters = baseParameters(productId).addValue("region", region);
        List<String> assignments = new ArrayList<>();

        if (request.name() != null) {
            assignments.add("name = :name");
            parameters.addValue("name", normalizeRequiredText(request.name(), "name"));
        }

        if (request.description() != null) {
            assignments.add("description = :description");
            parameters.addValue("description", normalizeOptionalText(request.description()));
        }

        if (request.price() != null) {
            assignments.add("price = :price");
            parameters.addValue("price", normalizePrice(request.price()));
        }

        if (request.stock() != null) {
            assignments.add("stock = :stock");
            parameters.addValue("stock", request.stock());
        }

        if (request.status() != null) {
            assignments.add("status = :status");
            parameters.addValue("status", ProductStatus.fromNullable(request.status()).name());
        }

        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("PATCH requiert au moins un champ modifiable.");
        }

        String sql = """
                UPDATE products
                SET %s
                WHERE id = :id
                  AND region = :region
                RETURNING id, region, name, description, price, stock, status, created_at, updated_at
                """.formatted(String.join(", ", assignments));

        return jdbcTemplate.query(sql, parameters, PRODUCT_ROW_MAPPER).stream().findFirst();
    }

    private MapSqlParameterSource baseParameters(UUID productId) {
        return new MapSqlParameterSource().addValue("id", productId);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " est obligatoire.");
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal normalizePrice(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("price est obligatoire.");
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
