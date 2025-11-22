CREATE OR REPLACE FUNCTION random_ts() RETURNS timestamp AS
$$
	SELECT (TIMESTAMP '2000-01-01' + (random() * (TIMESTAMP '2024-12-31' - TIMESTAMP '2000-01-01')))::date + time '12:00:00';
$$
LANGUAGE sql;

DELETE FROM bid_file;
DELETE FROM departure_bid;
DELETE FROM room_change_bid;
DELETE FROM notification;
DELETE FROM bid;
DELETE FROM event;
DELETE FROM resident;
DELETE FROM room;
DELETE FROM university_dormitory;
DELETE FROM dormitory;
DELETE FROM university;
DELETE FROM usr;

INSERT INTO usr (login, password, name, surname, role) VALUES
('nonResident1', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Иван', 'Иванов', 'NON_RESIDENT'),
('manager1', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Иван', 'Иванов', 'MANAGER'),
('manager2', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Иван', 'Иванов', 'MANAGER'),
('guard1', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Пётр', 'Петрович', 'GUARD');

INSERT INTO usr (login, password, name, surname, role) SELECT
	'resident' || i,
	'1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f',
	'Иван',
	'Иванов',
	'RESIDENT'
FROM generate_series(1, 1000) i;

INSERT INTO university (id, name, address) VALUES 
(nextval('university_id_seq'), 'ITMO', 'Кронверский пр., д.49'),
(nextval('university_id_seq'), 'СПГУ', 'ул. Спортивная, д.6'),
(nextval('university_id_seq'), 'ЛЭТИ', 'ул. Профессора Попова, д.5');

INSERT INTO dormitory (id, address) VALUES 
(nextval('dormitory_id_seq'), 'Вяземский пер., д.5-7'),
(nextval('dormitory_id_seq'), 'ул. Бассейная, д.8'),
(nextval('dormitory_id_seq'), 'Альпийский пер., д.15,к.2');

INSERT INTO university_dormitory (university_id, dormitory_id) VALUES
(1, 1), (1, 2), (1, 3), (2, 2), (2, 3), (3, 2), (3, 1);

INSERT INTO room (id, dormitory_id, number, type, capacity, floor, cost) SELECT
	nextval('room_id_seq'),
	(i / 200) + 1,
	(i % 200) + 1,
	CASE (floor(random() * 2)) 
		WHEN 0 THEN 'AISLE'
		WHEN 1 THEN 'BLOCK'
	END,
	floor(random() * 2) + 2,
	ceil(random() * 7),
	floor(random() * 5000) + 500
FROM generate_series(0, 599) i;

WITH data AS (
    SELECT
        'resident' || i AS login,
        ceil(random() * 3) AS uid
    FROM generate_series(1, 1000) i
)
INSERT INTO resident (login, university_id, room_id) SELECT
	d.login,
	d.uid,
	(SELECT r.id
	FROM room r
	JOIN university_dormitory ud USING(dormitory_id)
	WHERE ud.university_id = d.uid AND NOT is_room_filled(r.id)
	LIMIT 1)
FROM data d;

INSERT INTO event (id, type, timestamp, usr, room_id) SELECT
	nextval('event_id_seq'),
	'OCCUPATION',
	random_ts(),
	'resident' || (i + 1),
	(i / 2) + 1
FROM generate_series(0, 999) i;

INSERT INTO event (id, type, timestamp, usr) SELECT
	nextval('event_id_seq'),
	CASE i % 2
		WHEN 0 THEN 'IN'
		WHEN 1 THEN 'OUT'
	END,
	random_ts(),
	'resident' || (i / 1000) + 1
FROM generate_series(0, 999999) i;

INSERT INTO event (id, type, timestamp, usr, room_id, payment_sum) SELECT
	nextval('event_id_seq'),
	'PAYMENT',
	random_ts(),
	'resident' || (i / 100) + 1,
	(i / 200) + 1,
	floor(random() * 2500) + 500
FROM generate_series(0, 99999) i;

INSERT INTO bid(id, type, text, sender, status) VALUES
(1, 'OCCUPATION', 'Мне негде жить( Под мостом уже холодно по ночам', 'nonResident1', 'IN_PROCESS'),
(2, 'DEPARTURE', 'Отъезд домой', 'resident1', 'IN_PROCESS');

INSERT INTO occupation_bid(id, university_id, dormitory_id) VALUES
(1, 1, 1);

INSERT INTO departure_bid(id, day_from, day_to) VALUES
(2, NOW()::DATE, (NOW() + INTERVAL '1 month')::DATE);