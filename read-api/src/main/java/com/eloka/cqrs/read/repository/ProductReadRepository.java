package com.eloka.cqrs.read.repository;

import com.eloka.cqrs.common.domain.DeploymentRegion;
import com.eloka.cqrs.common.domain.ProductStatus;
import com.eloka.cqrs.read.dto.ProductViewResponse;
import org.springframework.beans.factory.annotation.Value;
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
            rs.getString("region"),
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
    private final DeploymentRegion deploymentRegion;

    public ProductReadRepository(NamedParameterJdbcTemplate jdbcTemplate, @Value("${cqrs.region}") String regionCode) {
        this.jdbcTemplate = jdbcTemplate;
        this.deploymentRegion = DeploymentRegion.fromCode(regionCode);
    }

    public List<ProductViewResponse> findAll() {
        return jdbcTemplate.query("""
                SELECT id,
                       region,
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
                WHERE region = :region
                ORDER BY updated_at DESC
                """, new MapSqlParameterSource("region", deploymentRegion.code()), PRODUCT_VIEW_ROW_MAPPER);
    }

    public Optional<ProductViewResponse> findById(UUID productId) {
        return jdbcTemplate.query("""
                SELECT id,
                       region,
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
                  AND region = :region
                """, new MapSqlParameterSource()
                .addValue("id", productId)
                .addValue("region", deploymentRegion.code()), PRODUCT_VIEW_ROW_MAPPER).stream().findFirst();
    }
}
