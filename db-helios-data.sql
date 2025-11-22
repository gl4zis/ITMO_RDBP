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
('misha', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Миша', 'Начинкин', 'NON_RESIDENT'),
('stas', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Стас', 'Щетинин', 'NON_RESIDENT'),
('kirill', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Кирилл', 'Марков', 'NON_RESIDENT'),
('roma', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Рома', 'Макеев', 'NON_RESIDENT'),
('manager1', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Иван', 'Ванко', 'MANAGER'),
('manager2', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Тони', 'Карк', 'MANAGER'),
('guard1', '1ab60e110d41a9aac5e30d086c490819bfe3461b38c76b9602fe9686aa0aa3d28c63c96a1019e3788c40a14f4292e50f', 'Пётр', 'Васильевич', 'GUARD');

INSERT INTO university (id, name, address) VALUES 
(nextval('university_id_seq'), 'ITMO', 'Кронверский пр., д.49'),
(nextval('university_id_seq'), 'СПГУ', 'ул. Спортивная, д.6'),
(nextval('university_id_seq'), 'ЛЭТИ', 'ул. Профессора Попова, д.5');

INSERT INTO dormitory (id, address) VALUES 
(nextval('dormitory_id_seq'), 'Вяземский пер., д.5-7'),
(nextval('dormitory_id_seq'), 'ул. Бассейная, д.8'),
(nextval('dormitory_id_seq'), 'Альпийский пер., д.15,к.2');

INSERT INTO university_dormitory (university_id, dormitory_id) VALUES
(1, 1), (1, 2), (1, 3), (2, 2), (2, 3), (3, 2);

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