-- The requirements state that identifiers are "long incremental": widen SERIAL (int4) columns to BIGINT.
ALTER TABLE users DROP CONSTRAINT fk_room;

ALTER TABLE rooms ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE rooms_id_seq AS BIGINT;

ALTER TABLE users ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE users_id_seq AS BIGINT;
ALTER TABLE users ALTER COLUMN room_id TYPE BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE SET NULL;

-- Users are always looked up inside a room
CREATE INDEX idx_users_room_id ON users (room_id);

