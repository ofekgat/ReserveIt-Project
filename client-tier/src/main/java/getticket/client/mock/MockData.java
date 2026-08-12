package getticket.client.mock;

import getticket.client.model.Booking;
import getticket.client.model.BookingSummary;
import getticket.client.model.EventInstance;
import getticket.client.model.Review;
import getticket.client.model.Seat;
import getticket.client.model.Show;
import getticket.client.model.Ticket;
import getticket.client.model.User;
import getticket.client.model.Venue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The client tier's entire "database" — a handful of static, hardcoded
 * lists held in memory for the lifetime of the running server.
 *
 * This class stands in for the real DAOs + MySQL. It exists purely so the
 * XHTML pages and backing beans have something realistic to render and
 * interact with while the Application/Data tiers are being built. It is
 * NOT thread-safe in any serious sense (booking methods are synchronized
 * as a simple safeguard, nothing more), it never persists to disk, and it
 * resets to the seed data below every time the app restarts.
 *
 * When the real server tier is ready, the backing beans in getticket.client.web
 * should be pointed at real DAOs/BookingService instead of this class — the
 * method shapes here were kept close to the real ones for that reason.
 */
public final class MockData {

    private MockData() {
    }

    // ---- Seed reference data (venues, seats, shows, event instances) ----
    // Venues/shows/instances are mutable CopyOnWriteArrayLists (not List.of()) so the
    // admin screens can create/edit/delete them at runtime, same as the real DAOs would.

    private static final AtomicInteger NEXT_VENUE_ID = new AtomicInteger(3);
    private static final List<Venue> VENUES = new CopyOnWriteArrayList<>(List.of(
            new Venue(1, "Main Hall", true, 40),
            new Venue(2, "Open Field Arena", false, 500)
    ));

    /** Seats per row when laying out a new seat grid; the last row may be shorter. */
    public static final int DEFAULT_SEATS_PER_ROW = 10;

    private static final AtomicInteger NEXT_SEAT_ID = new AtomicInteger(41);
    private static final List<Seat> SEATS = new CopyOnWriteArrayList<>(buildSeatsForVenue1());

    private static List<Seat> buildSeatsForVenue1() {
        List<Seat> seats = new ArrayList<>();
        int seatId = 1;
        for (int row = 1; row <= 4; row++) {
            for (int num = 1; num <= 10; num++) {
                seats.add(new Seat(seatId++, 1, row, num));
            }
        }
        return seats;
    }

    /**
     * Lays out `count` seats for a venue, `seatsPerRow` to a row, in the rows
     * following `rowOffset` (0 to start at row 1).
     *
     * A numbered venue with no seats renders an empty, unbookable seat map, so
     * every numbered venue must get a grid when it is created or resized.
     */
    private static void buildSeatGrid(int vid, int count, int seatsPerRow, int rowOffset) {
        int perRow = seatsPerRow > 0 ? seatsPerRow : DEFAULT_SEATS_PER_ROW;
        for (int i = 0; i < count; i++) {
            SEATS.add(new Seat(NEXT_SEAT_ID.getAndIncrement(), vid,
                    rowOffset + (i / perRow) + 1, (i % perRow) + 1));
        }
    }

    private static final AtomicInteger NEXT_SHOW_ID = new AtomicInteger(6);
    private static final List<Show> SHOWS = new CopyOnWriteArrayList<>(List.of(
            new Show(1, "Hamilton", "The story of American founding father Alexander Hamilton, told through hip-hop, jazz and R&B.", "Musical",
                    "https://images.unsplash.com/photo-1503095396549-807759245b35?w=400"),
            new Show(2, "The Nutcracker", "The classic Tchaikovsky ballet, performed by the city ballet company.", "Ballet",
                    "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400"),
            new Show(3, "Jazz Night Live", "An open-air evening of jazz standards with a rotating lineup of local musicians.", "Concert",
                    "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=400"),
            new Show(4, "Comedy Store Live", "Stand-up from five touring comedians, one mic, one night.", "Comedy",
                    "https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=400"),
            new Show(5, "Romeo & Juliet", "Shakespeare's tragedy of star-crossed lovers, in modern dress.", "Theatre",
                    "https://images.unsplash.com/photo-1507924538820-ede94a04019d?w=400")
    ));

    // Event instances are mutable (Available_tickets changes as bookings come in),
    // so this list is a CopyOnWriteArrayList rather than an immutable List.of().
    private static final AtomicInteger NEXT_INSTANCE_ID = new AtomicInteger(7);
    private static final List<EventInstance> INSTANCES = new CopyOnWriteArrayList<>(List.of(
            new EventInstance(1, 1, 1, LocalDateTime.of(2026, 8, 10, 19, 0), 120.00, 40, "SCHEDULED"),
            new EventInstance(2, 1, 1, LocalDateTime.of(2026, 8, 11, 19, 0), 120.00, 40, "SCHEDULED"),
            new EventInstance(3, 2, 1, LocalDateTime.of(2026, 8, 15, 18, 0), 85.00, 40, "SCHEDULED"),
            new EventInstance(4, 3, 2, LocalDateTime.of(2026, 8, 9, 20, 0), 45.00, 500, "SCHEDULED"),
            new EventInstance(5, 4, 2, LocalDateTime.of(2026, 8, 12, 21, 0), 35.00, 500, "SCHEDULED"),
            new EventInstance(6, 5, 1, LocalDateTime.of(2026, 8, 20, 19, 30), 95.00, 40, "SCHEDULED")
    ));

    // ---- Mutable "tables" seeded with a bit of demo history ----

    private static final AtomicInteger NEXT_USER_ID = new AtomicInteger(5);
    private static final List<User> USERS = new CopyOnWriteArrayList<>(List.of(
            new User(1, "demo", "demo123", "demo@getticket.example", "CUSTOMER"),
            new User(2, "alice", "alice123", "alice@getticket.example", "CUSTOMER"),
            new User(3, "ben", "ben123", "ben@getticket.example", "CUSTOMER"),
            new User(4, "admin", "admin123", "admin@getticket.example", "ADMIN")
    ));

    private static final AtomicInteger NEXT_BOOKING_ID = new AtomicInteger(3);
    private static final List<Booking> BOOKINGS = new CopyOnWriteArrayList<>(List.of(
            new Booking(1, 1, LocalDateTime.of(2026, 7, 20, 14, 5), 240.00, "CONFIRMED"),
            new Booking(2, 1, LocalDateTime.of(2026, 7, 25, 9, 30), 135.00, "CONFIRMED")
    ));

    private static final AtomicInteger NEXT_TICKET_ID = new AtomicInteger(6);
    private static final List<Ticket> TICKETS = new CopyOnWriteArrayList<>(List.of(
            new Ticket(1, 1, 1, 5),
            new Ticket(2, 1, 1, 6),
            new Ticket(3, 2, 4, null),
            new Ticket(4, 2, 4, null),
            new Ticket(5, 2, 4, null)
    ));

    private static final AtomicInteger NEXT_REVIEW_ID = new AtomicInteger(4);
    private static final List<Review> REVIEWS = new CopyOnWriteArrayList<>(List.of(
            new Review(1, 1, 2, "alice", 5, "Incredible show, worth every penny!"),
            new Review(2, 1, 3, "ben", 4, "Great energy, our seats were a bit far from the stage."),
            new Review(3, 3, 2, "alice", 5, "Best jazz night I've been to in the city.")
    ));

    // ---- Shows / catalog ----

    public static List<Show> getAllShows() {
        return SHOWS.stream().sorted(Comparator.comparing(Show::getSname)).collect(Collectors.toList());
    }

    public static Show getShowById(int sid) {
        return SHOWS.stream().filter(s -> s.getSid() == sid).findFirst().orElse(null);
    }

    public static List<Show> getShowsByCategory(String category) {
        if (category == null || category.isBlank()) {
            return getAllShows();
        }
        return SHOWS.stream()
                .filter(s -> s.getCategory().equalsIgnoreCase(category.trim()))
                .sorted(Comparator.comparing(Show::getSname))
                .collect(Collectors.toList());
    }

    public static List<Show> searchShowsByName(String nameFragment) {
        if (nameFragment == null || nameFragment.isBlank()) {
            return getAllShows();
        }
        String needle = nameFragment.trim().toLowerCase();
        return SHOWS.stream()
                .filter(s -> s.getSname().toLowerCase().contains(needle))
                .sorted(Comparator.comparing(Show::getSname))
                .collect(Collectors.toList());
    }

    /**
     * Catalog search with every filter applied together — any of them may be
     * null/blank to mean "don't narrow by this".
     */
    public static List<Show> searchShows(String nameFragment, String category, LocalDate date) {
        Set<Integer> sidsOnDate = date == null ? null : INSTANCES.stream()
                .filter(i -> i.getStartTime().toLocalDate().equals(date))
                .map(EventInstance::getSid)
                .collect(Collectors.toSet());
        String needle = nameFragment == null ? "" : nameFragment.trim().toLowerCase();

        return SHOWS.stream()
                .filter(s -> needle.isEmpty() || s.getSname().toLowerCase().contains(needle))
                .filter(s -> category == null || category.isBlank()
                        || s.getCategory().equalsIgnoreCase(category.trim()))
                .filter(s -> sidsOnDate == null || sidsOnDate.contains(s.getSid()))
                .sorted(Comparator.comparing(Show::getSname))
                .collect(Collectors.toList());
    }

    /** Distinct categories actually in use, for the catalog's category dropdown. */
    public static List<String> getAllCategories() {
        return SHOWS.stream().map(Show::getCategory).distinct().sorted().collect(Collectors.toList());
    }

    /** Distinct dates that have at least one non-cancelled showtime, oldest first. */
    public static List<LocalDate> getScheduledDates() {
        return INSTANCES.stream()
                .filter(i -> !"CANCELLED".equalsIgnoreCase(i.getEventStatus()))
                .map(i -> i.getStartTime().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<Show> getShowsByDate(LocalDate date) {
        if (date == null) {
            return getAllShows();
        }
        Set<Integer> sidsOnDate = INSTANCES.stream()
                .filter(i -> i.getStartTime().toLocalDate().equals(date))
                .map(EventInstance::getSid)
                .collect(Collectors.toSet());
        return SHOWS.stream()
                .filter(s -> sidsOnDate.contains(s.getSid()))
                .sorted(Comparator.comparing(Show::getSname))
                .collect(Collectors.toList());
    }

    // ---- Shows (admin) ----

    public static synchronized Show createShow(String sname, String description, String category, String imageUrl) {
        Show show = new Show(NEXT_SHOW_ID.getAndIncrement(), sname, description, category, imageUrl);
        SHOWS.add(show);
        return show;
    }

    /** Returns false if no show with that id exists. */
    public static synchronized boolean updateShow(int sid, String sname, String description, String category, String imageUrl) {
        Show existing = getShowById(sid);
        if (existing == null) {
            return false;
        }
        existing.setSname(sname);
        existing.setDescription(description);
        existing.setCategory(category);
        existing.setImageUrl(imageUrl);
        return true;
    }

    public static synchronized boolean deleteShow(int sid) {
        INSTANCES.removeIf(i -> i.getSid() == sid);
        return SHOWS.removeIf(s -> s.getSid() == sid);
    }

    // ---- Venues / seats / event instances ----

    public static List<Venue> getAllVenues() {
        return VENUES.stream().sorted(Comparator.comparing(Venue::getVname)).collect(Collectors.toList());
    }

    public static Venue getVenueById(int vid) {
        return VENUES.stream().filter(v -> v.getVid() == vid).findFirst().orElse(null);
    }

    public static String venueNameFor(int vid) {
        Venue venue = getVenueById(vid);
        return venue != null ? venue.getVname() : ("Venue #" + vid);
    }

    // ---- Venues (admin) ----

    /** Creates a venue and, when it uses numbered seating, its full seat grid. */
    public static synchronized Venue createVenue(String vname, boolean numbered, int vcapacity, int seatsPerRow) {
        Venue venue = new Venue(NEXT_VENUE_ID.getAndIncrement(), vname, numbered, vcapacity);
        VENUES.add(venue);
        if (numbered) {
            buildSeatGrid(venue.getVid(), vcapacity, seatsPerRow, 0);
        }
        return venue;
    }

    /**
     * Updates a venue and brings its seat map back in line with its capacity —
     * comparing against the venue's capacity rather than its previous values, so
     * a seat map that drifted out of sync is repaired on the next save. Growing
     * only appends rows, leaving existing seats (and their tickets) in place.
     */
    public static synchronized boolean updateVenue(int vid, String vname, boolean numbered,
                                                    int vcapacity, int seatsPerRow) {
        Venue existing = getVenueById(vid);
        if (existing == null) {
            return false;
        }

        existing.setVname(vname);
        existing.setNumbered(numbered);
        existing.setVcapacity(vcapacity);

        int currentSeatCount = getSeatsByVenue(vid).size();
        int targetSeatCount = numbered ? vcapacity : 0;

        if (targetSeatCount < currentSeatCount) {
            SEATS.removeIf(s -> s.getVid() == vid);
            if (targetSeatCount > 0) {
                buildSeatGrid(vid, targetSeatCount, seatsPerRow, 0);
            }
        } else if (targetSeatCount > currentSeatCount) {
            int startRow = getSeatsByVenue(vid).stream().mapToInt(Seat::getRowNum).max().orElse(0);
            buildSeatGrid(vid, targetSeatCount - currentSeatCount, seatsPerRow, startRow);
        }
        return true;
    }

    public static synchronized boolean deleteVenue(int vid) {
        SEATS.removeIf(s -> s.getVid() == vid);
        return VENUES.removeIf(v -> v.getVid() == vid);
    }

    // ---- Event instances (admin) ----

    public static synchronized EventInstance createInstance(int sid, int vid, LocalDateTime startTime,
                                                              double ticketPrice, int availableTickets, String eventStatus) {
        EventInstance instance = new EventInstance(
                NEXT_INSTANCE_ID.getAndIncrement(), sid, vid, startTime, ticketPrice, availableTickets, eventStatus);
        INSTANCES.add(instance);
        return instance;
    }

    public static synchronized boolean updateInstance(int instanceId, int vid, LocalDateTime startTime,
                                                        double ticketPrice, int availableTickets, String eventStatus) {
        EventInstance existing = getInstanceById(instanceId);
        if (existing == null) {
            return false;
        }
        existing.setVid(vid);
        existing.setStartTime(startTime);
        existing.setTicketPrice(ticketPrice);
        existing.setAvailableTickets(availableTickets);
        existing.setEventStatus(eventStatus);
        return true;
    }

    public static synchronized boolean deleteInstance(int instanceId) {
        return INSTANCES.removeIf(i -> i.getInstanceId() == instanceId);
    }

    public static EventInstance getInstanceById(int instanceId) {
        return INSTANCES.stream().filter(i -> i.getInstanceId() == instanceId).findFirst().orElse(null);
    }

    public static List<EventInstance> getInstancesByShow(int sid) {
        return INSTANCES.stream()
                .filter(i -> i.getSid() == sid)
                .sorted(Comparator.comparing(EventInstance::getStartTime))
                .collect(Collectors.toList());
    }

    public static List<Seat> getSeatsByVenue(int vid) {
        return SEATS.stream().filter(s -> s.getVid() == vid).collect(Collectors.toList());
    }

    /** Ids of seats already ticketed for this instance. */
    public static synchronized Set<Integer> getTakenSeatIds(int instanceId) {
        return TICKETS.stream()
                .filter(t -> t.getInstanceId() == instanceId && t.getSeatId() != null)
                .map(Ticket::getSeatId)
                .collect(Collectors.toSet());
    }

    /** The instance venue's whole seat map, in row/seat order — taken seats included. */
    public static synchronized List<Seat> getSeatMap(int instanceId) {
        EventInstance instance = getInstanceById(instanceId);
        if (instance == null) {
            return List.of();
        }
        return getSeatsByVenue(instance.getVid()).stream()
                .sorted(Comparator.comparingInt(Seat::getRowNum).thenComparingInt(Seat::getSeatNum))
                .collect(Collectors.toList());
    }

    /** Seats of the instance's venue that have no ticket yet for this instance. */
    public static synchronized List<Seat> getAvailableSeats(int instanceId) {
        Set<Integer> takenSeatIds = getTakenSeatIds(instanceId);
        return getSeatMap(instanceId).stream()
                .filter(s -> !takenSeatIds.contains(s.getSeatId()))
                .collect(Collectors.toList());
    }

    // ---- Users / auth (mock only — plaintext passwords, in-memory registration) ----

    public static User getUserById(int uid) {
        return USERS.stream().filter(u -> u.getUid() == uid).findFirst().orElse(null);
    }

    public static User getUserByUsername(String uname) {
        if (uname == null) {
            return null;
        }
        return USERS.stream().filter(u -> u.getUname().equalsIgnoreCase(uname.trim())).findFirst().orElse(null);
    }

    /** Returns the new User, or null if the username is already taken. */
    public static synchronized User registerUser(String uname, String password, String email) {
        if (getUserByUsername(uname) != null) {
            return null;
        }
        User user = new User(NEXT_USER_ID.getAndIncrement(), uname.trim(), password, email, "CUSTOMER");
        USERS.add(user);
        return user;
    }

    // ---- Bookings / checkout ----

    public static List<Booking> getBookingsByUser(int uid) {
        return BOOKINGS.stream()
                .filter(b -> b.getUid() == uid)
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .collect(Collectors.toList());
    }

    public static List<Ticket> getTicketsByBooking(int bookingId) {
        return TICKETS.stream().filter(t -> t.getBookingId() == bookingId).collect(Collectors.toList());
    }

    /** Booking history enriched with show/venue/time for display on myBookings.xhtml. */
    public static List<BookingSummary> getBookingSummariesByUser(int uid) {
        return summarize(getBookingsByUser(uid));
    }

    /** Every booking in the system, enriched the same way, newest first. For admin order tracking. */
    public static List<BookingSummary> getAllBookingSummaries() {
        List<Booking> all = new ArrayList<>(BOOKINGS);
        all.sort(Comparator.comparing(Booking::getBookingTime).reversed());
        return summarize(all);
    }

    private static List<BookingSummary> summarize(List<Booking> bookings) {
        List<BookingSummary> summaries = new ArrayList<>();
        for (Booking booking : bookings) {
            List<Ticket> tickets = getTicketsByBooking(booking.getBookingId());
            if (tickets.isEmpty()) {
                continue;
            }
            EventInstance instance = getInstanceById(tickets.get(0).getInstanceId());
            Show show = instance != null ? getShowById(instance.getSid()) : null;
            summaries.add(new BookingSummary(
                    booking,
                    show != null ? show.getSname() : "Unknown show",
                    instance != null ? venueNameFor(instance.getVid()) : "Unknown venue",
                    instance != null ? instance.getStartTime() : null,
                    tickets.size()));
        }
        return summaries;
    }

    /** Admin: overrides a booking's status (e.g. to CANCELLED). Returns false if no such booking exists. */
    public static synchronized boolean updateBookingStatus(int bookingId, String status) {
        Booking booking = BOOKINGS.stream().filter(b -> b.getBookingId() == bookingId).findFirst().orElse(null);
        if (booking == null) {
            return false;
        }
        booking.setStatus(status);
        return true;
    }

    /**
     * Books the given seats for uid. Verifies each seat is still free, creates
     * the Booking + one Ticket per seat, and decrements Available_tickets —
     * same shape as the real BookingService.checkout(), minus the actual
     * transaction/row-locking (there is only ever one in-memory "connection").
     */
    public static synchronized Booking bookSeats(int uid, int instanceId, List<Integer> seatIds)
            throws SeatUnavailableException {
        EventInstance instance = getInstanceById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Event instance " + instanceId + " does not exist");
        }

        Set<Integer> takenSeatIds = TICKETS.stream()
                .filter(t -> t.getInstanceId() == instanceId && t.getSeatId() != null)
                .map(Ticket::getSeatId)
                .collect(Collectors.toSet());
        List<Integer> alreadyBooked = seatIds.stream().filter(takenSeatIds::contains).collect(Collectors.toList());
        if (!alreadyBooked.isEmpty()) {
            throw new SeatUnavailableException(alreadyBooked);
        }

        double totalPrice = instance.getTicketPrice() * seatIds.size();
        Booking booking = new Booking(NEXT_BOOKING_ID.getAndIncrement(), uid, LocalDateTime.now(), totalPrice, "CONFIRMED");
        BOOKINGS.add(booking);

        for (int seatId : seatIds) {
            TICKETS.add(new Ticket(NEXT_TICKET_ID.getAndIncrement(), booking.getBookingId(), instanceId, seatId));
        }
        instance.setAvailableTickets(instance.getAvailableTickets() - seatIds.size());

        return booking;
    }

    /** Books ticketQuantity general-admission tickets (no seat) for uid. */
    public static synchronized Booking bookGeneralAdmission(int uid, int instanceId, int ticketQuantity)
            throws InsufficientTicketsException {
        EventInstance instance = getInstanceById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Event instance " + instanceId + " does not exist");
        }
        if (instance.getAvailableTickets() < ticketQuantity) {
            throw new InsufficientTicketsException(ticketQuantity, instance.getAvailableTickets());
        }

        double totalPrice = instance.getTicketPrice() * ticketQuantity;
        Booking booking = new Booking(NEXT_BOOKING_ID.getAndIncrement(), uid, LocalDateTime.now(), totalPrice, "CONFIRMED");
        BOOKINGS.add(booking);

        for (int i = 0; i < ticketQuantity; i++) {
            TICKETS.add(new Ticket(NEXT_TICKET_ID.getAndIncrement(), booking.getBookingId(), instanceId, null));
        }
        instance.setAvailableTickets(instance.getAvailableTickets() - ticketQuantity);

        return booking;
    }

    // ---- Reviews ----

    public static List<Review> getReviewsByShow(int sid) {
        return REVIEWS.stream()
                .filter(r -> r.getSid() == sid)
                .sorted(Comparator.comparingInt(Review::getReviewId).reversed())
                .collect(Collectors.toList());
    }

    public static double averageRating(int sid) {
        List<Review> reviews = getReviewsByShow(sid);
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    public static synchronized Review addReview(int sid, int uid, String uname, int rating, String comment) {
        Review review = new Review(NEXT_REVIEW_ID.getAndIncrement(), sid, uid, uname, rating, comment);
        REVIEWS.add(review);
        return review;
    }
}
