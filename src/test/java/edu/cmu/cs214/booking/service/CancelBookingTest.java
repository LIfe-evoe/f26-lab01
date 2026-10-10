package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void cancellingPromotesTheWaitlistedUser() { // S3
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        Booking a = assertInstanceOf(BookingResult.Confirmed.class, r).booking();
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 700)));
        svc.cancelBooking(a.id());
        assertEquals(1, svc.listBookings(roomA).size());
        Booking promoted = svc.listBookings(roomA).get(0);
        assertEquals(bob, promoted.user());
        assertEquals(new TimeInterval(630, 700), promoted.interval());
        assertTrue(store.waitlistForRoom(roomA).isEmpty());
    }

    @Test
    void cancellingPromotesAtMostOneWaiter() { // S4
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        Booking a = assertInstanceOf(BookingResult.Confirmed.class, r).booking();
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 700)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, charlie, new TimeInterval(640, 700)));
        svc.cancelBooking(a.id());
        // 只晋升最早排队的 bob；charlie 仍在候补
        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob, svc.listBookings(roomA).get(0).user());
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(charlie, store.waitlistForRoom(roomA).get(0).user());
    }

    @Test
    void cancellingSkipsStillConflictingWaiters() { // S5
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        Booking a = assertInstanceOf(BookingResult.Confirmed.class, r).booking();
        assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, dave, new TimeInterval(700, 760)));
        // bob [630,730) 与 dave [700,760) 冲突，应被跳过；eve [610,650) 不冲突，应晋升
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 730)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, eve, new TimeInterval(610, 650)));
        svc.cancelBooking(a.id());
        assertEquals(2, svc.listBookings(roomA).size()); // dave + eve
        assertTrue(svc.listBookings(roomA).stream()
                .anyMatch(b -> b.user().equals(eve)));
        assertEquals(1, store.waitlistForRoom(roomA).size()); // bob 仍被跳过留在候补
        assertEquals(bob, store.waitlistForRoom(roomA).get(0).user());
    }

    @Test
    void cancellingDoesNotPromoteAcrossRooms() { // S6
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        Booking a = assertInstanceOf(BookingResult.Confirmed.class, r).booking();
        assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomB, dave, new TimeInterval(600, 660)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomB, bob, new TimeInterval(630, 700)));
        svc.cancelBooking(a.id()); // 取消 roomA 的 alice
        // roomB 的候补与确认预订均不受影响
        assertEquals(1, store.waitlistForRoom(roomB).size());
        assertEquals(bob, store.waitlistForRoom(roomB).get(0).user());
        assertEquals(1, svc.listBookings(roomB).size());
        assertEquals(dave, svc.listBookings(roomB).get(0).user());
    }
}
