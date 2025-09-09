# --- !Ups

CREATE TYPE task_status AS ENUM ('active', 'archived', 'deleted');

ALTER TABLE tasks DROP COLUMN IF EXISTS project_id;
ALTER TABLE tasks DROP COLUMN IF EXISTS assigned_to;
ALTER TABLE tasks ADD COLUMN "status" task_status DEFAULT 'active';
ALTER TABLE tasks ADD COLUMN "is_completed" BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_column_id FOREIGN KEY (column_id) REFERENCES "columns"(id);

CREATE TABLE user_tasks (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    assigned_to INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by INT,
    CONSTRAINT fk_user_tasks_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tasks_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tasks_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
);

ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id);

ALTER TABLE user_workspaces
ADD CONSTRAINT fk_user_workspaces_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_workspaces
ADD CONSTRAINT fk_user_workspaces_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;

# --- !Downs

ALTER TABLE user_workspaces
DROP CONSTRAINT IF EXISTS fk_user_workspaces_user,
DROP CONSTRAINT IF EXISTS fk_user_workspaces_workspace;

ALTER TABLE users
DROP CONSTRAINT IF EXISTS fk_users_role;

DROP TABLE IF EXISTS user_tasks;

ALTER TABLE tasks
ADD COLUMN IF NOT EXISTS project_id INT,
ADD COLUMN IF NOT EXISTS assigned_to INT,
DROP COLUMN IF EXISTS status,
DROP COLUMN IF EXISTS is_completed,
DROP CONSTRAINT IF EXISTS fk_tasks_column_id
;

DROP TYPE IF EXISTS task_status;

