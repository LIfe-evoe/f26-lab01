package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

class IsAvailableTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");

    @Test
    void notAvailableWhenAnEarlierBookingRunsIntoTheRequest() {
        // 已有预订 E=[570,630) 先开始、延伸进查询区间 Q=[600,660)：真实冲突
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(570, 630));
        assertFalse(svc.isAvailable(roomA, new TimeInterval(600, 660)));
    }

    @Test
    void notAvailableWhenAnExistingBookingContainsTheRequest() {
        // 已有预订 E=[540,720) 完全包含查询区间 Q=[600,660)：真实冲突
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(540, 720));
        assertFalse(svc.isAvailable(roomA, new TimeInterval(600, 660)));
    }

    @Test
    void isAvailableAgreesWithBook() {
        // isAvailable 为 true 时 book 应确认；为 false 时 book 应进候补
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        assertTrue(svc.isAvailable(roomA, new TimeInterval(600, 660)));
        assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(600, 660)));

        assertFalse(svc.isAvailable(roomA, new TimeInterval(610, 650)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, alice, new TimeInterval(610, 650)));
    }
}
