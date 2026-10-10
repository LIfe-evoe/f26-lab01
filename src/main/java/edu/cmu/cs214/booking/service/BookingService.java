package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import edu.cmu.cs214.booking.repo.BookingStore;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates bookings and the waitlist. Enforces the core invariant: a room
 * never holds two confirmed bookings whose intervals overlap. Persistence is
 * delegated to a {@link BookingStore}.
 */
public class BookingService {

    private final BookingStore store;
    private int nextBookingSeq = 1;
    private int nextWaitlistSeq = 1;

    public BookingService(BookingStore store) {
        this.store = store;
    }

    /**
     * Attempts to book {@code room} for {@code user} over {@code interval}. If the
     * room is free over that interval the booking is confirmed; otherwise the user
     * is placed on the room's waitlist.
     */
    public BookingResult book(Room room, User user, TimeInterval interval) {
        for (Booking existing : store.bookingsForRoom(room)) {
            if (existing.interval().overlaps(interval)) {
                int position = store.waitlistForRoom(room).size() + 1;
                int seq = nextWaitlistSeq++;
                store.addWaitlistEntry(new WaitlistEntry("w" + seq, room, user, interval, seq));
                return new BookingResult.Waitlisted(position);
            }
        }
        Booking booking = new Booking("b" + nextBookingSeq++, room, user, interval);
        store.addBooking(booking);
        return new BookingResult.Confirmed(booking);
    }


    /**
     * Reports whether {@code room} is free over {@code interval}, so callers can
     * check availability before attempting to book.
     */
    public boolean isAvailable(Room room, TimeInterval interval) {
        for (Booking b : store.bookingsForRoom(room)) {
            if (b.interval().overlaps(interval)) {
                return false;
            }
        }
        return true;
    }

    /** Returns the confirmed bookings for {@code room}. */
    public List<Booking> listBookings(Room room) {
        return store.bookingsForRoom(room);
    }

    /**
     * Cancels the confirmed booking with the given id, freeing its slot.
     * Unknown ids are ignored. The earliest fitting waitlisted user (by
     * {@code seq}) for the same room is promoted; at most one is promoted.
     */
    public void cancelBooking(String bookingId) {
        Optional<Booking> maybeBooking = store.findBooking(bookingId);
        if (maybeBooking.isEmpty()) {
            return; // R3: unknown id, do nothing
        }
        Booking cancelled = maybeBooking.get();
        store.removeBooking(bookingId);

        List<WaitlistEntry> waiters = store.waitlistForRoom(cancelled.room()).stream()
                .sorted(java.util.Comparator.comparingInt(WaitlistEntry::seq))
                .toList();
        for (WaitlistEntry waiter : waiters) {
            boolean conflict = store.bookingsForRoom(cancelled.room()).stream()
                    .anyMatch(existing -> existing.interval().overlaps(waiter.interval()));
            if (!conflict) {
                store.addBooking(new Booking("b" + nextBookingSeq++, waiter.room(), waiter.user(), waiter.interval()));
                store.removeWaitlistEntry(waiter.id());
                return; // R5: promote at most one
            }
            // R6: skip conflicting waiters, consider next
        }
    }
}
