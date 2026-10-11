CREATE TABLE stock (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    batch_number varchar(255) NOT NULL UNIQUE ,
    expiry_date date NOT NULL,
    quantity int NOT NULL,
    reserved_quantity int NOT NULL,
    created_at timestamp NOT NULL,
    product_id UUID NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id)
)