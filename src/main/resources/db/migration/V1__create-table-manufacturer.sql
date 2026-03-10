CREATE TABLE manufacturer (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name varchar(225) NOT NULL,
    cnpj varchar(255) UNIQUE NOT NULL,
    created_at timestamp NOT NULL,
    active boolean NOT NULL
);