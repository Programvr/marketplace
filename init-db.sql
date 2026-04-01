CREATE SCHEMA IF NOT EXISTS marketplace;

CREATE TABLE IF NOT EXISTS marketplace.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS marketplace.products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES marketplace.users(id)
);

CREATE INDEX idx_products_seller_id ON marketplace.products(seller_id);
CREATE INDEX idx_users_email ON marketplace.users(email);

INSERT INTO marketplace.users (email, name, user_type)
VALUES ('vendedor@marketplace.com', 'Vendedor Ejemplo', 'SELLER')
ON CONFLICT (email) DO NOTHING;

INSERT INTO marketplace.users (email, name, user_type)
VALUES ('comprador@marketplace.com', 'Comprador Ejemplo', 'BUYER')
ON CONFLICT (email) DO NOTHING;