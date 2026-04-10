CREATE TABLE location_images (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(255) NOT NULL,
    is_cover BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    location_id BIGINT NOT NULL,

    CONSTRAINT fk_location_image_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE INDEX idx_location_image_location_id ON location_images(location_id);