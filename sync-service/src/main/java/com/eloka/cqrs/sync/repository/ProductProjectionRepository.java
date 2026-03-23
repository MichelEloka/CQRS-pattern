package com.eloka.cqrs.sync.repository;

import com.eloka.cqrs.common.events.ProductSnapshot;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductProjectionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductProjectionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(ProductSnapshot snapshot) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", snapshot.id())
                .addValue("name", snapshot.name())
                .addValue("description", snapshot.description())
                .addValue("price", snapshot.price())
                .addValue("stock", snapshot.stock())
                .addValue("status", snapshot.status().name())
                .addValue("createdAt", snapshot.createdAt())
                .addValue("updatedAt", snapshot.updatedAt());

        jdbcTemplate.update("""
                INSERT INTO product_projection (
                    id,
                    name,
                    description,
                    price,
                    stock,
                    status,
                    created_at,
                    updated_at,
                    synced_at
                )
                VALUES (
                    :id,
                    :name,
                    :description,
                    :price,
                    :stock,
                    :status,
                    :createdAt,
                    :updatedAt,
                    NOW()
                )
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    description = EXCLUDED.description,
                    price = EXCLUDED.price,
                    stock = EXCLUDED.stock,
                    status = EXCLUDED.status,
                    created_at = EXCLUDED.created_at,
                    updated_at = EXCLUDED.updated_at,
                    synced_at = NOW()
                WHERE product_projection.updated_at <= EXCLUDED.updated_at
                """, parameters);
    }
}

