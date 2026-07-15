CREATE TABLE contract_items (
    id UUID DEFAULT  RANDOM_UUID() PRIMARY KEY,
    unit_price decimal(10, 2) NOT NULL,
    total_quantity int NOT NULL,
    balance_quantity int NOT NULL,
    created_at timestamp NOT NULL,
    contract_id UUID NOT NULL,
    product_id UUID NOT NULL,
    FOREIGN KEY (contract_id) REFERENCES contract(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)