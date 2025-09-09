# add description, updatedBy, and isDeleted columns to the workspaces table

# --- !Ups
ALTER TABLE workspaces
ADD COLUMN IF NOT EXISTS description VARCHAR(255);

ALTER TABLE workspaces
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE workspaces
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT false;

# --- !Downs
ALTER TABLE workspaces
DROP COLUMN IF EXISTS description;

ALTER TABLE workspaces
DROP COLUMN IF EXISTS updated_by;

ALTER TABLE workspaces
DROP COLUMN IF EXISTS is_deleted;