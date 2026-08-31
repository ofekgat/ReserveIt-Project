# ReserveIt / GetTicket – Ticket Booking System

A ticket booking system built on a three-tier architecture (JSF + Java + MySQL), written as a workshop project for an advanced Java programming course.

## What's in this repo

| Directory | What it is | Artifact |
|---|---|---|
| `src/` | **Application Tier** – the full application: models, JDBC DAOs, services, backing beans and XHTML screens, running against a real MySQL database | `get-ticket.war` |
| `client-tier/` | **Client Tier** – a working mockup backed by an in-memory `MockData` class, no database (see `client-tier/README.md`) | `get-ticket-client.war` |
| `database/` | **Data Tier** – `schema.sql` (full schema with foreign keys and constraints) and `seed_data.sql` (demo data) | — |

## Running locally

Requirements: Java 17, Maven, MySQL 8, Tomcat 9.

The steps are the same on every platform; only the deploy command differs, so
both variants are given below. `CATALINA_HOME` is the Tomcat installation
directory — the one containing `bin/` and `webapps/`.

**1. Create the database**

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p getticket < database/seed_data.sql
```

`schema.sql` creates the `getticket` database and drops any existing tables — never point it at a database holding real data.
`seed_data.sql` loads a demo catalog: 4 cities, 3 numbered venues with seat maps, 11 shows and 21 showtimes.

**2. Connection settings**

Connection details are read from `src/main/resources/db.properties`, which is
the only source `ConnectionPool` looks at — edit it before building:

```properties
db.url=jdbc:mysql://localhost:3306/getticket?useSSL=false&serverTimezone=UTC
db.user=root
db.password=yourpassword
```

The file is packaged into the WAR at build time, so re-run `mvn clean package`
after changing it. Note that the comment at the top of that file mentions
overriding these values with `DB_URL` / `DB_USER` / `DB_PASSWORD` environment
variables — that is not implemented; `ConnectionPool` never reads the
environment.

**3. Build and deploy**

macOS/Linux:

```bash
mvn clean package
cp target/get-ticket.war $CATALINA_HOME/webapps/
```

Windows (PowerShell):

```powershell
mvn clean package
Copy-Item target\get-ticket.war $env:CATALINA_HOME\webapps\
```

Windows (Command Prompt):

```bat
mvn clean package
copy target\get-ticket.war %CATALINA_HOME%\webapps\
```

Start Tomcat with `$CATALINA_HOME/bin/startup.sh` on macOS/Linux, or
`%CATALINA_HOME%\bin\startup.bat` on Windows. The application then comes up at
http://localhost:8080/get-ticket/

The client mockup builds separately (`cd client-tier` then `mvn clean package`) and deploys the same way as `get-ticket-client.war`.

## Admin account

The seed data ships an admin account you can log in with straight away:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |

This is a **local development demo account** whose hash is written into `seed_data.sql` — replace it in any real environment.

The users `yossi` and `dana` are demo customers that exist so the seeded bookings have owners; their `Password` column holds a placeholder string rather than a valid hash, so **they cannot log in**. New users sign up through the registration form on the login screen, and their password is stored as a hash via `PasswordUtil` (PBKDF2WithHmacSHA256, 65536 iterations, random salt).

## Screens

**Customer**: catalog with search by name, category and date → show details and showtime selection → seat selection (a graphical seat map for numbered venues) → booking confirmation.

**Admin** (guarded by `AdminFilter`, reachable only with the ADMIN role):

- **Venues** – create and edit venues; creating a numbered venue also lays out its seat map through `VenueService`.
- **Shows & showtimes** – manage shows, and for each one its schedule (venue, time, price, ticket count, status).
- **Order tracking** – every booking in the system, with status updates.
- **Sales reports** – filter by date range and status, with revenue and ticket totals broken down by show, venue and status.

## Project layout

```
src/main/java/getticket/
├── model/    – POJOs: Location, User, Show, Venue, Seat, EventInstance, Booking, Ticket, Review
├── dao/      – DAO interfaces plus JDBC implementations (dao/impl), all using PreparedStatement
├── service/  – business logic: BookingService (atomic transactional booking, double-booking
│               prevention), VenueService (venue plus seat map), OrderReportService (order data
│               and reporting)
├── util/     – ConnectionPool, PasswordUtil
└── web/      – backing beans (Catalog, Checkout, UserSession, Admin*) plus AuthFilter/AdminFilter

src/main/webapp/          – XHTML screens, shared template and CSS
database/                 – schema.sql + seed_data.sql
```

## Technical notes

- **JSF 2.2 (Mojarra), not 2.3**: Tomcat is a plain servlet container with no CDI, and Mojarra 2.3 fails there with `Unable to find CDI BeanManager`. A consequence is that `f:convertDateTime` has no built-in `java.time` support, so dates are formatted by hand in the backing beans with `DateTimeFormatter`.
- **Double booking is prevented by the database itself**, through the `UNIQUE (Instance_id, Seat_id)` constraint on the `Tickets` table — not by application code alone.
- **Show times are stored as wall-clock values**: the `Start_time` and `Booking_time` columns map directly to `LocalDateTime` rather than through `java.sql.Timestamp`, so the driver performs no time zone conversion between the database and the server and the screens show exactly what is stored.
- **Prices are displayed in shekels (₪).**
