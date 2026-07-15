CREATE TABLE contract (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    contract_number varchar(255) UNIQUE NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status varchar(50) NOT NULL,
    created_at timestamp NOT NULL,
    client_id UUID NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id)
)