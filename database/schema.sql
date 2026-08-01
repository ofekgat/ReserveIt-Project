-- ============================================================
-- GetTicket - Database Schema
-- Target: MySQL 8.0+ / MariaDB 10.2+
-- Engine: InnoDB (required for transactions and foreign keys)
-- Charset: utf8mb4 (full Hebrew support)
-- ============================================================

CREATE DATABASE IF NOT EXISTS getticket
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE getticket;

-- Drop in reverse dependency order so the script can be re-run
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS Tickets;
DROP TABLE IF EXISTS Bookings;
DROP TABLE IF EXISTS Event_Instances;
DROP TABLE IF EXISTS Seats;
DROP TABLE IF EXISTS Venues;
DROP TABLE IF EXISTS Shows;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Locations;


-- ------------------------------------------------------------
-- 1. Locations
-- ------------------------------------------------------------
CREATE TABLE Locations (
    Location_id INT AUTO_INCREMENT PRIMARY KEY,
    City        VARCHAR(60)  NOT NULL,
    Address     VARCHAR(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 2. Users
-- Password stores a HASH, never plaintext -> needs 255 chars
-- ------------------------------------------------------------
CREATE TABLE Users (
    Uid      INT AUTO_INCREMENT PRIMARY KEY,
    Uname    VARCHAR(50)  NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Email    VARCHAR(120) NOT NULL,
    Role     ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',

    CONSTRAINT uq_users_uname UNIQUE (Uname),
    CONSTRAINT uq_users_email UNIQUE (Email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 3. Shows
-- ------------------------------------------------------------
CREATE TABLE Shows (
    Sid         INT AUTO_INCREMENT PRIMARY KEY,
    Sname       VARCHAR(150) NOT NULL,
    Description TEXT,
    Category    VARCHAR(60)  NOT NULL,
    ImageUrl    VARCHAR(500),

    INDEX idx_shows_category (Category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 4. Venues
-- IsNumbered = TRUE  -> assigned seating (Seats rows exist)
-- IsNumbered = FALSE -> general admission (counter only)
-- ------------------------------------------------------------
CREATE TABLE Venues (
    Vid         INT AUTO_INCREMENT PRIMARY KEY,
    Location_id INT         NOT NULL,
    Vname       VARCHAR(100) NOT NULL,
    IsNumbered  BOOLEAN     NOT NULL DEFAULT TRUE,
    Vcapacity   INT         NOT NULL,

    CONSTRAINT fk_venues_location
        FOREIGN KEY (Location_id) REFERENCES Locations(Location_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_venues_capacity CHECK (Vcapacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 5. Seats
-- Physical seats. Only for venues where IsNumbered = TRUE.
-- ------------------------------------------------------------
CREATE TABLE Seats (
    Seat_id  INT AUTO_INCREMENT PRIMARY KEY,
    Vid      INT NOT NULL,
    Row_num  INT NOT NULL,
    Seat_num INT NOT NULL,

    CONSTRAINT fk_seats_venue
        FOREIGN KEY (Vid) REFERENCES Venues(Vid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    -- The same physical seat cannot be defined twice in one venue
    CONSTRAINT uq_seats_position UNIQUE (Vid, Row_num, Seat_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 6. Event_Instances
-- A specific show, in a specific venue, at a specific time.
-- ------------------------------------------------------------
CREATE TABLE Event_Instances (
    Instance_id      INT AUTO_INCREMENT PRIMARY KEY,
    Sid              INT           NOT NULL,
    Vid              INT           NOT NULL,
    Start_time       DATETIME      NOT NULL,
    Ticket_price     DECIMAL(10,2) NOT NULL,
    Available_tickets INT          NOT NULL,
    Event_Status     ENUM('SCHEDULED','CANCELLED','POSTPONED','SOLD_OUT')
                     NOT NULL DEFAULT 'SCHEDULED',

    CONSTRAINT fk_instances_show
        FOREIGN KEY (Sid) REFERENCES Shows(Sid)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_instances_venue
        FOREIGN KEY (Vid) REFERENCES Venues(Vid)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_instances_price CHECK (Ticket_price >= 0),
    -- Guard against overselling in general-admission venues
    CONSTRAINT chk_instances_available CHECK (Available_tickets >= 0),

    INDEX idx_instances_start (Start_time),
    INDEX idx_instances_show_time (Sid, Start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 7. Bookings
-- One transaction / cart made by a customer.
-- ------------------------------------------------------------
CREATE TABLE Bookings (
    Booking_id   INT AUTO_INCREMENT PRIMARY KEY,
    Uid          INT           NOT NULL,
    Booking_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Total_price  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    Status       ENUM('PENDING','CONFIRMED','PAID','CANCELLED') NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (Uid) REFERENCES Users(Uid)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_bookings_total CHECK (Total_price >= 0),

    INDEX idx_bookings_user (Uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 8. Tickets
-- One row = one ticket.
--
-- *** THE MOST IMPORTANT CONSTRAINT IN THIS SCHEMA ***
-- uq_tickets_instance_seat makes double-booking physically
-- impossible: the database itself refuses the second write.
--
-- Seat_id is NULL for general-admission venues. In MySQL and
-- MariaDB, NULLs count as distinct inside a UNIQUE index, so
-- many NULL-seat tickets for the same instance are still allowed.
-- ------------------------------------------------------------
CREATE TABLE Tickets (
    Ticket_id   INT AUTO_INCREMENT PRIMARY KEY,
    Booking_id  INT NOT NULL,
    Instance_id INT NOT NULL,
    Seat_id     INT NULL,

    CONSTRAINT fk_tickets_booking
        FOREIGN KEY (Booking_id) REFERENCES Bookings(Booking_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_instance
        FOREIGN KEY (Instance_id) REFERENCES Event_Instances(Instance_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_seat
        FOREIGN KEY (Seat_id) REFERENCES Seats(Seat_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT uq_tickets_instance_seat UNIQUE (Instance_id, Seat_id),

    INDEX idx_tickets_booking (Booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ------------------------------------------------------------
-- 9. Reviews
-- One review per user per show.
-- ------------------------------------------------------------
CREATE TABLE Reviews (
    Review_id INT AUTO_INCREMENT PRIMARY KEY,
    Sid       INT     NOT NULL,
    Uid       INT     NOT NULL,
    Rating    TINYINT NOT NULL,
    Comment   TEXT,

    CONSTRAINT fk_reviews_show
        FOREIGN KEY (Sid) REFERENCES Shows(Sid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (Uid) REFERENCES Users(Uid)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_reviews_rating CHECK (Rating BETWEEN 1 AND 5),
    CONSTRAINT uq_reviews_user_show UNIQUE (Sid, Uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
