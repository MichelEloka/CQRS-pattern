package com.eloka.cqrs.read.repository;

import com.eloka.cqrs.common.domain.ProductStatus;
import com.eloka.cqrs.read.dto.ProductViewResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductReadRepository {

    private static final RowMapper<ProductViewResponse> PRODUCT_VIEW_ROW_MAPPER = (rs, rowNum) -> new ProductViewResponse(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"),
            ProductStatus.valueOf(rs.getString("status")),
            rs.getString("availability"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class),
            rs.getObject("synced_at", OffsetDateTime.class)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductViewResponse> findAll() {
        return jdbcTemplate.query("""
                SELECT id,
                       name,
                       description,
                       price,
                       stock,
                       status,
                       availability,
                       created_at,
                       updated_at,
                       synced_at
                FROM product_catalog_view
                ORDER BY updated_at DESC
                """, PRODUCT_VIEW_ROW_MAPPER);
    }

    public Optional<ProductViewResponse> findById(UUID productId) {
        return jdbcTemplate.query("""
                SELECT id,
                       name,
                       description,
                       price,
                       stock,
                       status,
                       availability,
                       created_at,
                       updated_at,
                       synced_at
                FROM product_catalog_view
                WHERE id = :id
                """, new MapSqlParameterSource("id", productId), PRODUCT_VIEW_ROW_MAPPER).stream().findFirst();
    }
}

