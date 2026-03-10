CREATE TABLE product(
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name varchar(255) NOT NULL ,
    sku varchar(255) UNIQUE NOT NULL,
    unit_of_measure varchar(255) NOT NULL,
    dosage varchar(255) NOT NULL,
    created_at timestamp NOT NULL,
    active boolean NOT NULL,
    manufacturer_id UUID NOT NULL ,
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturer(id) ON DELETE CASCADE,
);