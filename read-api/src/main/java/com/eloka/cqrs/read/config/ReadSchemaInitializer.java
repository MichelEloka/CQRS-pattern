package com.eloka.cqrs.read.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReadSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public ReadSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS product_projection (
                    id UUID PRIMARY KEY,
                    region TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
                    stock INTEGER NOT NULL CHECK (stock >= 0),
                    status TEXT NOT NULL CHECK (status IN ('AVAILABLE', 'LIMITED', 'DISCONTINUED')),
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL,
                    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);

        jdbcTemplate.execute("ALTER TABLE product_projection ADD COLUMN IF NOT EXISTS region TEXT");
        jdbcTemplate.execute("UPDATE product_projection SET region = 'eu-west' WHERE region IS NULL OR region = ''");
        jdbcTemplate.execute("ALTER TABLE product_projection ALTER COLUMN region SET NOT NULL");

        jdbcTemplate.execute("DROP VIEW IF EXISTS product_catalog_view");

        jdbcTemplate.execute("""
                CREATE VIEW product_catalog_view AS
                SELECT
                    id,
                    region,
                    name,
                    description,
                    price,
                    stock,
                    status,
                    CASE
                        WHEN status = 'DISCONTINUED' THEN 'discontinued'
                        WHEN stock = 0 THEN 'out-of-stock'
                        WHEN stock < 5 THEN 'low-stock'
                        ELSE 'in-stock'
                    END AS availability,
                    created_at,
                    updated_at,
                    synced_at
                FROM product_projection
                """);
    }
}
