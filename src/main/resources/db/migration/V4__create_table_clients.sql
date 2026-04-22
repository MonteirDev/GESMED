CREATE TABLE clients(
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name varchar(255) NOT NULL,
    cnpj varchar(255) UNIQUE NOT NULL,
    created_at timestamp NOT NULL,
);