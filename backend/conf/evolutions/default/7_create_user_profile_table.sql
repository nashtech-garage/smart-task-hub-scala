CREATE TABLE user_profiles (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_language VARCHAR(10) NOT NULL DEFAULT 'en',
    theme_mode VARCHAR(10) NOT NULL DEFAULT 'Light',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_profile_user_id_idx UNIQUE (user_id)
);
