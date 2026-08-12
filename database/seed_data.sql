-- ============================================================
-- GetTicket - Demo data
-- Run AFTER schema.sql
-- ============================================================

-- ---------- Locations ----------
INSERT INTO Locations (City, Address) VALUES
('תל אביב',  'שדרות רוטשילד 22'),
('חיפה',     'דרך העצמאות 15'),
('ירושלים',  'יפו 97');

-- ---------- Users ----------
-- NOTE: these are placeholder hash strings, not real hashes.
-- Real values will come from PasswordUtil.
INSERT INTO Users (Uname, Password, Email, Role) VALUES
('admin', '65536:KtgqM2XPBgUZTlB90DXvvg==:HOWtXUT6qaTeAh0UamIvj/eXl9WfEjFkD2vZ6T+Y6NQ=', 'admin@getticket.co.il', 'ADMIN'),
('yossi', '$2a$10$PLACEHOLDER_HASH_YOSSI', 'yossi@example.com',     'CUSTOMER'),
('dana',  '$2a$10$PLACEHOLDER_HASH_DANA',  'dana@example.com',      'CUSTOMER');

-- ---------- Shows ----------
INSERT INTO Shows (Sname, Description, Category, ImageUrl) VALUES
('הקול בראש 2', 'סרט אנימציה על רגשות בגיל ההתבגרות', 'סרט ילדים',    'images/inside_out_2.jpg'),
('פסטיגל 2026', 'מופע ראווה מוזיקלי לכל המשפחה',      'מופע ילדים',   'images/festigal.jpg'),
('סטנדאפ בלילה', 'מופע סטנדאפ למבוגרים',              'מופע סטנדאפ',  'images/standup.jpg');

-- ---------- Venues ----------
-- Vid 1: numbered seating (3 rows x 5 seats = 15 seats)
-- Vid 2: general admission, no seat map
INSERT INTO Venues (Location_id, Vname, IsNumbered, Vcapacity) VALUES
(1, 'אולם VIP',   TRUE,  15),
(2, 'במת דשא',    FALSE, 500);

-- ---------- Seats (venue 1 only) ----------
INSERT INTO Seats (Vid, Row_num, Seat_num) VALUES
(1,1,1),(1,1,2),(1,1,3),(1,1,4),(1,1,5),
(1,2,1),(1,2,2),(1,2,3),(1,2,4),(1,2,5),
(1,3,1),(1,3,2),(1,3,3),(1,3,4),(1,3,5);

-- ---------- Event_Instances ----------
INSERT INTO Event_Instances
    (Sid, Vid, Start_time, Ticket_price, Available_tickets, Event_Status) VALUES
(1, 1, '2026-08-10 18:00:00',  40.00,  15, 'SCHEDULED'),
(1, 1, '2026-08-10 21:00:00',  40.00,  15, 'SCHEDULED'),
(2, 2, '2026-09-01 19:30:00',  80.00, 500, 'SCHEDULED');

-- ---------- Bookings + Tickets ----------
-- Yossi buys two adjacent numbered seats (row 1, seats 1-2)
INSERT INTO Bookings (Uid, Total_price, Status) VALUES (2, 80.00, 'PAID');
INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES
(1, 1, 1),
(1, 1, 2);
UPDATE Event_Instances SET Available_tickets = Available_tickets - 2
WHERE Instance_id = 1;

-- Dana buys three general-admission tickets (Seat_id stays NULL)
INSERT INTO Bookings (Uid, Total_price, Status) VALUES (3, 240.00, 'PAID');
INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES
(2, 3, NULL),
(2, 3, NULL),
(2, 3, NULL);
UPDATE Event_Instances SET Available_tickets = Available_tickets - 3
WHERE Instance_id = 3;

-- ---------- Reviews ----------
INSERT INTO Reviews (Sid, Uid, Rating, Comment) VALUES
(1, 2, 5, 'סרט מעולה, הילדים נהנו מאוד'),
(1, 3, 4, 'טוב, אבל קצת ארוך');


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


-- E) Rating out of range -> must FAIL on the CHECK constraint.
-- INSERT INTO Reviews (Sid, Uid, Rating) VALUES (2, 2, 9);
