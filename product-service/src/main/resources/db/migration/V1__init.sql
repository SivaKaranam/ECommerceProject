CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE INDEX idx_products_category_id ON products (category_id);

INSERT INTO categories (name) VALUES ('Electronics'), ('Books'), ('Home & Kitchen');

INSERT INTO products (name, description, price, stock_quantity, category_id) VALUES
    ('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 799.00, 150, 1),
    ('Mechanical Keyboard', 'RGB backlit mechanical keyboard, blue switches', 3499.00, 80, 1),
    ('Noise Cancelling Headphones', 'Over-ear headphones with active noise cancellation', 5999.00, 45, 1),
    ('Clean Code', 'A Handbook of Agile Software Craftsmanship', 899.00, 200, 2),
    ('Designing Data-Intensive Applications', 'The big ideas behind reliable, scalable systems', 1299.00, 60, 2),
    ('Non-Stick Frying Pan', '28cm non-stick frying pan with induction base', 1199.00, 100, 3);
