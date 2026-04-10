CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_location_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_location_user_id ON locations(user_id);
CREATE INDEX idx_location_coordinates ON locations(latitude, longitude);