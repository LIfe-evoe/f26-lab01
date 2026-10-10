package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

class CancelBookingTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");
    private final User charlie = new User("u3", "Charlie");
    private final User dave = new User("u4", "Dave");
    private final User eve = new User("u5", "Eve");

    @Test
    void cancellingARemovedBookingFreesTheRoom() { // S1
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        Booking a = assertInstanceOf(BookingResult.Confirmed.class, r).booking();
        svc.cancelBooking(a.id());
        assertEquals(0, svc.listBookings(roomA).size());
    }

    @Test
    void cancellingUnknownIdDoesNothing() { // S2
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(630, 700));
        svc.cancelBooking("no-such-id");
        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(1, store.waitlistForRoom(roomA).size());
    }
}
