-- Create names table
-- Using BIGSERIAL to match Hibernate's Long type (BIGINT)
CREATE TABLE IF NOT EXISTS names (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Insert initial names
INSERT INTO names (name) VALUES ('Alice');
INSERT INTO names (name) VALUES ('Bob');
INSERT INTO names (name) VALUES ('Charlie');
INSERT INTO names (name) VALUES ('Diana');
INSERT INTO names (name) VALUES ('Eve');

