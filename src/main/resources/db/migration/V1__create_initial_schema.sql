CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(120) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
                            id UUID PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(120) NOT NULL UNIQUE,
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
                          id UUID PRIMARY KEY,
                          category_id UUID NOT NULL REFERENCES categories(id),
                          name VARCHAR(150) NOT NULL,
                          description TEXT,
                          sku VARCHAR(60) NOT NULL UNIQUE,
                          price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
                          stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_active ON products(active);