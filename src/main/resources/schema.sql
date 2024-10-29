DROP TABLE IF EXISTS real_estate;
CREATE TABLE IF NOT EXISTS real_estate
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    address   VARCHAR(255) NOT NULL,
    price     NUMERIC(10, 2),
    rooms     INT,
    bathrooms INT,
    sq_meters DOUBLE PRECISION,
    CONSTRAINT unique_real_estate UNIQUE (name, address, price)
);
GRANT ALL PRIVILEGES ON TABLE real_estate TO testuser;
GRANT USAGE, SELECT ON SEQUENCE real_estate_id_seq TO testuser;