# --- !Ups

-- 1. ENUM declarations
CREATE TYPE workspace_status AS ENUM ('active', 'archived');
CREATE TYPE user_workspace_role AS ENUM ('admin', 'member');
CREATE TYPE user_workspace_status AS ENUM ('pending', 'active', 'inactive');
CREATE TYPE project_status AS ENUM ('active', 'completed', 'deleted');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE notification_type AS ENUM ('task_assigned', 'task_completed', 'deadline_approaching', 'comment_added', 'task_moved');

-- 2. Table definitions (now use created ENUMs)
CREATE TABLE "roles" (
  "id" serial PRIMARY KEY,
  "name" varchar UNIQUE
);

CREATE TABLE "users" (
  "id" serial PRIMARY KEY,
  "name" varchar,
  "email" varchar UNIQUE,
  "password" varchar,
  "avatar_url" text,
  "role_id" int,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "workspaces" (
  "id" serial PRIMARY KEY,
  "name" varchar,
  "status" workspace_status DEFAULT 'active',
  "created_by" int,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "user_workspaces" (
  "id" serial PRIMARY KEY,
  "user_id" int,
  "workspace_id" int,
  "role" user_workspace_role,
  "status" user_workspace_status DEFAULT 'active',
  "invited_by" int,
  "joined_at" timestamp
);

CREATE TABLE "projects" (
  "id" serial PRIMARY KEY,
  "name" varchar,
  "description" text,
  "workspace_id" int,
  "status" project_status DEFAULT 'active',
  "created_by" int,
  "updated_by" int,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "columns" (
  "id" serial PRIMARY KEY,
  "project_id" int,
  "name" varchar,
  "position" int,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "tasks" (
  "id" serial PRIMARY KEY,
  "project_id" int,
  "column_id" int,
  "name" varchar,
  "description" text,
  "start_date" timestamp,
  "end_date" timestamp,
  "priority" task_priority DEFAULT 'MEDIUM',
  "position" int,
  "assigned_to" int,
  "created_by" int,
  "updated_by" int,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "checklists" (
  "id" serial PRIMARY KEY,
  "task_id" int,
  "name" varchar,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "checklist_items" (
  "id" serial PRIMARY KEY,
  "checklist_id" int,
  "content" text,
  "is_completed" boolean DEFAULT false,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "task_comments" (
  "id" serial PRIMARY KEY,
  "task_id" int,
  "user_id" int,
  "content" text,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "tags" (
  "id" serial PRIMARY KEY,
  "project_id" int,
  "name" varchar,
  "color" varchar(7),
  "created_at" timestamp
);

CREATE TABLE "task_tags" (
  "id" serial PRIMARY KEY,
  "task_id" int,
  "tag_id" int
);

CREATE TABLE "notifications" (
  "id" serial PRIMARY KEY,
  "user_id" int,
  "task_id" int,
  "type" notification_type,
  "message" text,
  "is_read" boolean DEFAULT false,
  "created_at" timestamp
);

CREATE TABLE "activity_logs" (
  "id" serial PRIMARY KEY,
  "user_id" int,
  "project_id" int,
  "task_id" int,
  "action" varchar,
  "content" text,
  "created_at" timestamp
);

# --- !Downs

DROP TYPE IF EXISTS workspace_status;
DROP TYPE IF EXISTS user_workspace_role;
DROP TYPE IF EXISTS user_workspace_status;
DROP TYPE IF EXISTS project_status;
DROP TYPE IF EXISTS task_priority;
DROP TYPE IF EXISTS notification_type;

DROP TABLE IF EXISTS task_tags;
DROP TABLE IF EXISTS task_comments;
DROP TABLE IF EXISTS checklist_items;
DROP TABLE IF EXISTS checklists;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS columns;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS user_workspaces;
DROP TABLE IF EXISTS workspaces;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS activity_logs;
