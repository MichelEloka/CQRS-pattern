package com.eloka.cqrs.write.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WriteSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public WriteSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id UUID PRIMARY KEY,
                    region TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
                    stock INTEGER NOT NULL CHECK (stock >= 0),
                    status TEXT NOT NULL CHECK (status IN ('AVAILABLE', 'LIMITED', 'DISCONTINUED')),
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);

        jdbcTemplate.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS region TEXT");
        jdbcTemplate.execute("UPDATE products SET region = 'eu-west' WHERE region IS NULL OR region = ''");
        jdbcTemplate.execute("ALTER TABLE products ALTER COLUMN region SET NOT NULL");

        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION set_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql
                """);

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_products_updated_at ON products");
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_products_updated_at
                BEFORE UPDATE ON products
                FOR EACH ROW
                EXECUTE FUNCTION set_updated_at()
                """);
    }
}

