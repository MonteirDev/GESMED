CREATE TABLE supplier_purchase_history (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    batch_number VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    purchase_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    manufacturer_id UUID NOT NULL,
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturer(id),
    product_id UUID NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    stock_id UUID NOT NULL,
    FOREIGN KEY (stock_id) REFERENCES stock(id)
);