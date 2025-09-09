# --- !Ups

-- Create ENUM for project visibility
CREATE TYPE project_visibility AS ENUM ('public', 'private', 'workspace');
-- Create ENUM for user roles
CREATE TYPE user_role AS ENUM ('owner', 'member');

-- Add visibility column to projects table
ALTER TABLE projects
ADD COLUMN IF NOT EXISTS visibility project_visibility DEFAULT 'workspace';

-- drop description column from projects table
ALTER TABLE projects
DROP COLUMN IF EXISTS description;

-- Create user_projects table
CREATE TABLE user_projects (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    project_id INT NOT NULL,
    role user_role NOT NULL DEFAULT 'member',
    invited_by INT,
    joined_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_invited_by FOREIGN KEY (invited_by) REFERENCES users(id)
);

# --- !Downs

-- Remove visibility column from projects table
ALTER TABLE projects
DROP COLUMN IF EXISTS visibility;

-- Re-add description column to projects table
ALTER TABLE projects
ADD COLUMN IF NOT EXISTS description text;

-- Drop user_projects table
DROP TABLE IF EXISTS user_projects;

-- Drop ENUM types
DROP TYPE IF EXISTS project_visibility;
DROP TYPE IF EXISTS user_role;
