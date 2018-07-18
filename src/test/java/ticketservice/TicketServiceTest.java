package ticketservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TicketServiceTest {

    @Test
    void testHoldAndReserve() {
        TicketServiceImpl ts = new TicketServiceImpl();

        SeatHold s = ts.findAndHoldSeats(2, "jko@mail.com");
        assertEquals(2, s.seatCount());

        assertEquals(TicketServiceImpl.SEAT_COUNT-2, ts.numSeatsAvailable());

        ts.EXPIRATION_DELTA = 1000L;
        try {
            Thread.sleep (100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ts.reserveSeats(s.getId(), "jko@mail.com");

        assertEquals(TicketServiceImpl.SEAT_COUNT-2, ts.numSeatsAvailable());

    }

    @Test
    void testHoldAndExpire() {
        TicketServiceImpl ts = new TicketServiceImpl();

        SeatHold s = ts.findAndHoldSeats(2, "jko@mail.com");
        assertEquals(2, s.seatCount());

        assertEquals(TicketServiceImpl.SEAT_COUNT-2, ts.numSeatsAvailable());

        ts.EXPIRATION_DELTA = 1000L;
        try {
            Thread.sleep (2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ts.reserveSeats(s.getId(), "jko@mail.com");

        assertEquals(TicketServiceImpl.SEAT_COUNT, ts.numSeatsAvailable());

    }

    @Test
    void doBasics() {
        TicketServiceImpl ts = new TicketServiceImpl();

        assertEquals(TicketServiceImpl.SEAT_COUNT, ts.numSeatsAvailable());

        SeatHold s = ts.findAndHoldSeats(1, "jko@aaa.com");
        assertEquals(1, s.seatCount());
        assertEquals(new Integer(0), s.getSeats().get(0));
        assertEquals(TicketServiceImpl.SEAT_COUNT-1, ts.numSeatsAvailable());

        ts.EXPIRATION_DELTA = 1000L;
        try {
            Thread.sleep (2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(TicketServiceImpl.SEAT_COUNT, ts.numSeatsAvailable());
    }

}