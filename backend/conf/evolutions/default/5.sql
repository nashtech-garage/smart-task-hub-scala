# --- !Ups
CREATE TYPE column_status AS ENUM ('active', 'archived', 'deleted');

ALTER TABLE "columns"
ADD COLUMN "status" column_status DEFAULT 'active';

# --- !Downs
DROP TYPE column_status;

ALTER TABLE "columns"
DROP COLUMN "status";