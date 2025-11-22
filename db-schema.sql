-- DROP SCHEMA

DROP TRIGGER IF EXISTS validate_rcb_trigger ON room_change_bid;
DROP TRIGGER IF EXISTS create_new_bid_notifications_trigger ON bid;
DROP TRIGGER IF EXISTS check_room_occupancy_trigger ON resident;
DROP TRIGGER IF EXISTS room_change_auto_deny_trigger ON room_change_bid;
DROP TRIGGER IF EXISTS create_processed_bid_notification_trigger ON bid;

DROP FUNCTION IF EXISTS validate_rcb, create_new_bid_notifications, is_room_filled, check_room_occupancy,
	room_change_auto_deny, get_last_payment_time, calculate_resident_debt, create_processed_bid_notification,
	get_residents_to_eviction_by_debt;

DROP TABLE IF EXISTS bid_file, room_change_bid, departure_bid, occupation_bid, notification, bid, event,
	resident, room, university_dormitory, dormitory, university, usr;

-- CREATE SCHEMA

CREATE TABLE usr (
	login varchar PRIMARY KEY,
	password varchar NOT NULL,
	name varchar NOT NULL,
	surname varchar NOT NULL,
	role varchar NOT NULL, -- [SECURITY, MANAGER, NON_RESIDENT, RESIDENT]
	
	CONSTRAINT login_validation CHECK (login ~ '^[a-zA-Z0-9_]{3,20}$')
);

CREATE TABLE university (
	id serial PRIMARY KEY,
	name varchar NOT NULL,
	address varchar NOT NULL
);

CREATE TABLE dormitory (
	id serial PRIMARY KEY,
	address varchar NOT NULL
);

CREATE TABLE university_dormitory (
	university_id int,
	dormitory_id int,
	
	CONSTRAINT pk_ud PRIMARY KEY (university_id, dormitory_id),
	CONSTRAINT fk_ud_university FOREIGN KEY (university_id) REFERENCES university (id),
	CONSTRAINT fk_ud_dormitory FOREIGN KEY (dormitory_id) REFERENCES dormitory (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE room (
	id serial PRIMARY KEY,
	dormitory_id int NOT NULL,
	number int NOT NULL,
	type varchar NOT NULL, -- [AISLE, BLOCK]
	capacity int NOT NULL,
	floor int NOT NULL,
	cost int NOT NULL,
	
	CONSTRAINT unique_room_number UNIQUE (dormitory_id, number),
	CONSTRAINT fk_room_dormitory FOREIGN KEY (dormitory_id) REFERENCES dormitory (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT positive_number CHECK (number > 0),
	CONSTRAINT positive_capacity CHECK (capacity > 0),
	CONSTRAINT positive_cost CHECK (cost > 0)
);

CREATE TABLE resident (
	login varchar PRIMARY KEY,
	university_id int NOT NULL,
	room_id int NOT NULL,

	CONSTRAINT fk_res_usr FOREIGN KEY (login) REFERENCES usr (login),
	CONSTRAINT fk_res_university FOREIGN KEY (university_id) REFERENCES university (id),
	CONSTRAINT fk_resident_room FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE event (
	id bigserial PRIMARY KEY,
	type varchar NOT NULL, -- [PAYMENT, IN, OUT, OCCUPATION, EVICTION, ROOM_CHANGE]
	timestamp timestamp NOT NULL DEFAULT now(),
	usr varchar NOT NULL,
	room_id int,
	payment_sum int,
	
	CONSTRAINT fk_event_user FOREIGN KEY (usr) REFERENCES usr (login) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_event_room FOREIGN KEY (room_id) REFERENCES room (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE bid (
	id bigserial PRIMARY KEY,
	type varchar NOT NULL, -- [OCCUPATION, DEPARTURE, ROOM_CHANGE, EVICTION]
	text text NOT NULL,
	sender varchar NOT NULL,
	manager varchar,
	status varchar NOT NULL, -- [IN_PROCESS, PENDING_REVISION ACCEPTED, DENIED]
	event_id bigint,
	comment text,
	
	CONSTRAINT fk_bid_sender FOREIGN KEY (sender) REFERENCES usr (login),
	CONSTRAINT fk_bid_manager FOREIGN KEY (manager) REFERENCES usr (login),
	CONSTRAINT fk_bid_event FOREIGN KEY (event_id) REFERENCES event (id)
);

CREATE TABLE room_change_bid (
	id bigint PRIMARY KEY,
	room_to_id int,
	room_prefer_type varchar, -- [AISLE, BLOCK]
	
	CONSTRAINT fk_rcb_bid FOREIGN KEY (id) REFERENCES bid (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_rcb_room FOREIGN KEY (room_to_id) REFERENCES room (id)
);

CREATE TABLE bid_file (
	key varchar PRIMARY KEY,
	bid_id bigint,
	name varchar NOT NULL,
	
	CONSTRAINT fk_bf_bid FOREIGN KEY (bid_id) REFERENCES bid (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE departure_bid (
	id bigint PRIMARY KEY,
	day_from date NOT NULL,
	day_to date NOT NULL,
	
	CONSTRAINT fk_db_bid FOREIGN KEY (id) REFERENCES bid (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE occupation_bid (
	id bigint PRIMARY KEY,
	university_id int NOT NULL,
	dormitory_id int NOT NULL,
	
	CONSTRAINT fk_ob_bid FOREIGN KEY (id) REFERENCES bid (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_ob_university FOREIGN KEY (university_id) REFERENCES university (id),
	CONSTRAINT fk_ob_dormitory FOREIGN KEY (dormitory_id) REFERENCES dormitory (id)
);

CREATE TABLE notification (
	id bigserial PRIMARY KEY,
	bid_id bigint,
	receiver varchar NOT NULL,
	text text NOT NULL,
	status varchar NOT NULL DEFAULT 'CREATED', -- [CREATED, READ]
	
	CONSTRAINT fk_notification_bid FOREIGN KEY (bid_id) REFERENCES bid (id),
	CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver) REFERENCES usr (login)
);

-- TRIGGERS

CREATE OR REPLACE FUNCTION validate_rcb() RETURNS trigger AS
$$ BEGIN
	IF (NEW.room_to_id IS NULL AND NEW.room_prefer_type IS NULL) OR
		(NEW.room_to_id IS NOT NULL AND NEW.room_prefer_type IS NOT NULL)
	THEN
		RAISE EXCEPTION 'Room change bid % is invalid!', NEW.bid_id;
	END IF;
	RETURN NEW;
END; $$
LANGUAGE plpgsql;
-- validate room_change_bid
CREATE OR REPLACE TRIGGER validate_rcb_trigger
BEFORE INSERT OR UPDATE ON room_change_bid
FOR EACH ROW EXECUTE PROCEDURE validate_rcb();

CREATE OR REPLACE FUNCTION create_new_bid_notifications() RETURNS trigger AS
$$ BEGIN
	IF NEW.status = 'IN_PROCESS' THEN
		INSERT INTO notification (bid_id, receiver, text, status)
		SELECT NEW.id, login as receiver, 'Появилась новая заявка', 'CREATED'
		FROM usr
		WHERE role = 'MANAGER';
	END IF;
	RETURN NEW;
END; $$
LANGUAGE plpgsql;
-- create notification for managers on bid creation
CREATE OR REPLACE TRIGGER create_new_bid_notifications_trigger
AFTER INSERT ON bid
FOR EACH ROW EXECUTE PROCEDURE create_new_bid_notifications();

CREATE OR REPLACE FUNCTION create_processed_bid_notification() RETURNS trigger AS
$$ DECLARE
	status_text varchar;
BEGIN
	IF NEW.status = 'ACCEPTED' THEN status_text := 'одобрена :)';
	ELSEIF NEW.status = 'DENIED' THEN status_text := 'отклонена ;(';
	ELSE RETURN NEW;
	END IF;
	
	INSERT INTO notification (bid_id, receiver, text) VALUES
	(NEW.id, NEW.sender, format('Ваша заявка %s', status_text));
	RETURN NEW;
END; $$
LANGUAGE plpgsql;
-- create notification for accepter after bid processed
CREATE OR REPLACE TRIGGER create_processed_bid_notification_trigger
AFTER UPDATE ON bid
FOR EACH ROW EXECUTE PROCEDURE create_processed_bid_notification();

CREATE OR REPLACE FUNCTION is_room_filled(room_id int) RETURNS bool AS
$$
	SELECT r.capacity = (
		SELECT count(*)
		FROM resident res
		WHERE res.room_id = r.id
	)
	FROM room r
	WHERE r.id = room_id;
$$
LANGUAGE sql;

CREATE OR REPLACE FUNCTION check_room_occupancy() RETURNS trigger AS
$$ BEGIN	
	IF is_room_filled(NEW.room_id)
	THEN RAISE EXCEPTION 'Room capacity exceeded';
	END IF;
	RETURN NEW;
END; $$
LANGUAGE plpgsql;
-- check if room is full before move resident into it
CREATE OR REPLACE TRIGGER check_room_occupancy_trigger
BEFORE INSERT OR UPDATE ON resident
FOR EACH ROW EXECUTE PROCEDURE check_room_occupancy();

CREATE OR REPLACE FUNCTION room_change_auto_deny() RETURNS trigger AS
$$ BEGIN
	IF NEW.room_to_id IS NOT NULL AND is_room_filled(NEW.room_to_id) THEN
		UPDATE bid
		SET status = 'DENIED'
		WHERE id = NEW.bid_id;
	END IF;
	RETURN NEW;
END; $$
LANGUAGE plpgsql;
-- auto deny room_change_bid if room is filled
CREATE OR REPLACE TRIGGER room_change_auto_deny_trigger
BEFORE INSERT OR UPDATE ON room_change_bid
FOR EACH ROW EXECUTE PROCEDURE room_change_auto_deny();

-- FUNCTIONS

CREATE OR REPLACE FUNCTION get_last_payment_time(res varchar) RETURNS timestamp AS
$$
	SELECT max(timestamp)
	FROM event e
	WHERE (e.type = 'PAYMENT' OR e.type = 'OCCUPATION')
	AND e.usr = res;
$$
LANGUAGE sql;

CREATE OR REPLACE FUNCTION calculate_resident_debt(resident varchar) RETURNS int AS
$$ DECLARE
	last_payment_time timestamp;
	debt_for_months int;
	room_cost int;
BEGIN
	last_payment_time := COALESCE(get_last_payment_time(resident), '2000-01-01');
	SELECT 
		EXTRACT(YEAR FROM AGE(NOW(), last_payment_time)) * 12 +
		EXTRACT(MONTH FROM AGE(NOW(), last_payment_time))
	INTO debt_for_months;
	debt_for_months := debt_for_months;
	
	SELECT COALESCE(ro.cost, 0)
	INTO room_cost
	FROM room ro
	JOIN resident r ON r.room_id = ro.id
	WHERE r.login = resident;
	
	RETURN room_cost * debt_for_months;
END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_residents_to_eviction_by_debt() RETURNS TABLE (resident varchar) AS
$$
	SELECT r.login as resident
	FROM resident r
	WHERE (NOW() AT TIME ZONE 'UTC+3') - COALESCE(get_last_payment_time(r.login), '2000-01-01') > INTERVAL '6 months';
$$
LANGUAGE sql;

CREATE INDEX event_type ON event USING BTREE (type);
CREATE INDEX event_ts ON event USING BTREE (timestamp);