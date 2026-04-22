CREATE TABLE client_addresses(
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    label varchar(255) NOT NULL ,
    street varchar(255) NOT NULL,
    number varchar(255),
    complement varchar(255),
    neighborhood varchar(255),
    city varchar(255) NOT NULL,
    state varchar(255) NOT NULL,
    zip_code varchar(255) NOT NULL,
    is_main boolean default FALSE,
    created_at timestamp NOT NULL,
    client_id UUID NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);