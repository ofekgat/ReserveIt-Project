-- ============================================================
-- GetTicket - Demo data
-- Run AFTER schema.sql
--
-- Mirrors the catalog the app is demoed with: 4 locations,
-- 3 numbered venues, 11 shows and 21 showtimes, all in English.
-- ============================================================

-- ---------- Locations ----------
INSERT INTO Locations (City, Address) VALUES
('Tel Aviv',  '22 Rothschild Blvd'),
('Haifa',     '15 HaAtzmaut Road'),
('Jerusalem', '97 Jaffa Street'),
('Holon',     'Haatzmaut 50');

-- ---------- Users ----------
-- 'admin' is a real PasswordUtil (PBKDF2WithHmacSHA256) hash for password "admin123",
-- so the seeded admin account can actually log in. yossi/dana are still placeholders —
-- they're demo customers, not needed to exercise the admin area.
INSERT INTO Users (Uname, Password, Email, Role) VALUES
('admin', '65536:eyW1g/DGVcTEXhNjRLENMA==:+zpHn36Kd0QPJkI9rPIY8VEEkWN/2cKwB43ECngP2k8=', 'admin@getticket.co.il', 'ADMIN'),
('yossi', '$2a$10$PLACEHOLDER_HASH_YOSSI', 'yossi@example.com',     'CUSTOMER'),
('dana',  '$2a$10$PLACEHOLDER_HASH_DANA',  'dana@example.com',      'CUSTOMER');

-- ---------- Shows ----------
-- Sid 1..11 in insertion order; the Event_Instances below reference these numbers.
INSERT INTO Shows (Sname, Description, Category, ImageUrl) VALUES
('All in the head',   'animation about the puberty',                                  'Theatre', ''),
('Festigal fantasy',  'musical for all the family',                                   'Theatre', ''),
('STANDUP COMEDY',    'adults only standup 18+',                                      'Standup', ''),
('Hamilton',          'hamilton musical for all ages',                                'Musical', ''),
('The Odyssey',       'Best seller 2026',                                             'Theatre', ''),
('The Lion King',     'Disney''s classic brought to the stage, for the whole family',  'Musical', ''),
('Swan Lake',         'Tchaikovsky''s ballet performed by the national company',       'Ballet',  ''),
('Jazz Night Live',   'An evening of classic and modern jazz standards',              'Concert', ''),
('Improv Wars',       'Two teams, one stage, no script',                              'Standup', ''),
('Peter Pan',         'A flying adventure for children aged 4 and up',                'Family',  ''),
('Romeo and Juliet',  'Shakespeare''s tragedy in a modern staging',                   'Theatre', '');

-- ---------- Venues ----------
-- All three are numbered, so each one needs a matching seat grid below:
-- a venue with IsNumbered = TRUE and no Seats rows renders an empty,
-- unbookable seat map.
-- Vid 1: VIP Hall    -> 3 rows x 5  = 15 seats
-- Vid 2: Lawn Stage  -> 5 rows x 10 = 50 seats
-- Vid 3: HaCameri    -> 50 rows x 10 = 500 seats
INSERT INTO Venues (Location_id, Vname, IsNumbered, Vcapacity) VALUES
(4, 'VIP Hall',   TRUE,  15),
(4, 'Lawn Stage', TRUE,  50),
(4, 'HaCameri',   TRUE, 500);

-- ---------- Seats ----------
-- Generated rather than listed: HaCameri alone is 500 rows.
INSERT INTO Seats (Vid, Row_num, Seat_num)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 50
)
SELECT 1, r.n, s.n FROM nums r JOIN nums s ON s.n <= 5  WHERE r.n <= 3
UNION ALL
SELECT 2, r.n, s.n FROM nums r JOIN nums s ON s.n <= 10 WHERE r.n <= 5
UNION ALL
SELECT 3, r.n, s.n FROM nums r JOIN nums s ON s.n <= 10 WHERE r.n <= 50
ORDER BY 1, 2, 3;

-- ---------- Event_Instances ----------
-- Available_tickets starts at the venue capacity. An instance seeded with 0
-- can never be booked: checkout refuses to push the counter negative.
INSERT INTO Event_Instances
    (Sid, Vid, Start_time, Ticket_price, Available_tickets, Event_Status) VALUES
-- All in the head
(1,  1, '2026-08-10 18:00:00',  40.00,  15, 'SCHEDULED'),
(1,  1, '2026-08-10 21:00:00',  40.00,  15, 'SCHEDULED'),
-- Festigal fantasy
(2,  2, '2026-09-01 19:30:00',  80.00,  50, 'SCHEDULED'),
-- STANDUP COMEDY
(3,  1, '2026-09-12 21:00:00',  65.00,  15, 'SCHEDULED'),
(3,  2, '2026-10-03 21:30:00',  65.00,  50, 'SCHEDULED'),
-- Hamilton
(4,  3, '2027-02-21 19:00:00',  50.00, 500, 'SCHEDULED'),
(4,  1, '2027-02-22 19:00:00',  50.00,  15, 'SCHEDULED'),
-- The Odyssey
(5,  3, '2026-09-20 19:00:00',  90.00, 500, 'SCHEDULED'),
(5,  3, '2026-11-07 19:00:00',  90.00, 500, 'SCHEDULED'),
-- The Lion King
(6,  3, '2026-09-26 18:00:00', 120.00, 500, 'SCHEDULED'),
(6,  3, '2026-09-27 18:00:00', 120.00, 500, 'SCHEDULED'),
-- Swan Lake
(7,  3, '2026-10-10 20:00:00', 150.00, 500, 'SCHEDULED'),
(7,  3, '2026-10-11 17:00:00', 150.00, 500, 'SCHEDULED'),
-- Jazz Night Live
(8,  2, '2026-10-17 21:00:00',  75.00,  50, 'SCHEDULED'),
(8,  2, '2026-11-21 21:00:00',  75.00,  50, 'SCHEDULED'),
-- Improv Wars
(9,  1, '2026-09-19 22:00:00',  55.00,  15, 'SCHEDULED'),
(9,  1, '2026-10-24 22:00:00',  55.00,  15, 'SCHEDULED'),
-- Peter Pan
(10, 3, '2026-12-05 11:00:00',  70.00, 500, 'SCHEDULED'),
(10, 3, '2026-12-12 11:00:00',  70.00, 500, 'SCHEDULED'),
-- Romeo and Juliet
(11, 3, '2027-01-16 20:00:00',  95.00, 500, 'SCHEDULED'),
(11, 3, '2027-02-06 20:00:00',  95.00, 500, 'SCHEDULED');

-- ---------- Bookings + Tickets ----------
-- Yossi buys two adjacent seats at VIP Hall (row 1, seats 1-2 -> Seat_id 1, 2)
INSERT INTO Bookings (Uid, Total_price, Status) VALUES (2, 80.00, 'PAID');
INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES
(1, 1, 1),
(1, 1, 2);
UPDATE Event_Instances SET Available_tickets = Available_tickets - 2
WHERE Instance_id = 1;

-- Dana buys three seats at Lawn Stage (row 1, seats 1-3 -> Seat_id 16, 17, 18)
INSERT INTO Bookings (Uid, Total_price, Status) VALUES (3, 240.00, 'PAID');
INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES
(2, 3, 16),
(2, 3, 17),
(2, 3, 18);
UPDATE Event_Instances SET Available_tickets = Available_tickets - 3
WHERE Instance_id = 3;


-- ============================================================
-- VERIFICATION QUERIES
-- Run these one at a time to confirm the schema behaves correctly
-- ============================================================

-- A) Free seats for instance 1 (should return 13 of 15)
-- This is the query the DAO will need for the seat map.
SELECT s.Seat_id, s.Row_num, s.Seat_num
FROM Seats s
JOIN Event_Instances ei ON ei.Vid = s.Vid
LEFT JOIN Tickets t ON t.Seat_id = s.Seat_id
                   AND t.Instance_id = ei.Instance_id
WHERE ei.Instance_id = 1
  AND t.Ticket_id IS NULL
ORDER BY s.Row_num, s.Seat_num;


-- B) THE CRITICAL TEST: try to double-book seat 1 on instance 1.
-- This MUST fail with a duplicate-key error.
-- If it succeeds, the UNIQUE constraint is missing.
-- INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES (1, 1, 1);


-- C) Same seat, DIFFERENT instance -> must SUCCEED.
-- Seat 1 at the 21:00 show is a different product.
-- INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES (1, 2, 1);


-- D) Multiple NULL seats on one instance -> must SUCCEED.
-- Proves general admission still works under the UNIQUE constraint.
-- INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES (2, 3, NULL);
