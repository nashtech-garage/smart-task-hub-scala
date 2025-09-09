# --- !Ups

-- Add 2 default role : user and admin
INSERT INTO roles (name) VALUES ('user');
INSERT INTO roles (name) VALUES ('admin');

# --- !Downs

DELETE FROM roles WHERE name IN ('user', 'admin');
