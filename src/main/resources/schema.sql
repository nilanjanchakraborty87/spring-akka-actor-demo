DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS event_out_mapping;
CREATE TABLE event (id VARCHAR(64) PRIMARY KEY, schema_class VARCHAR(30), message VARBINARY(max));
CREATE TABLE event_out_mapping (id VARCHAR(64) PRIMARY KEY, event_id VARCHAR(64), out_message VARBINARY(max));