package ticketservice;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class TicketServiceImpl implements TicketService {
    public static Long EXPIRATION_DELTA = 10000L;

    public static int ROWS = 9;
    public static int COLS = 33;
    public static int SEAT_COUNT = ROWS * COLS;

    public static final String OPEN = "open";
    public static final String HELD = "heald";
    public static final String RESERVED = "reserved";

    private String[] openSeats = new String[SEAT_COUNT];
    {
        for (int i=0; i < SEAT_COUNT; i++) {
            openSeats[i] = OPEN;
        }
    }

    private long[] notExpire = new long[SEAT_COUNT];
    {
        for (int i=0; i < SEAT_COUNT; i++) {
            notExpire[i] = Long.MAX_VALUE;
        }
    }

    private final IntegerCounter availableSeats = new IntegerCounter(SEAT_COUNT);

    private final AtomicLongArray seatsTimestamp = new AtomicLongArray(notExpire);
    private final AtomicReferenceArray<String> seats = new AtomicReferenceArray<>(openSeats);

    private final ConcurrentMap<Integer, SeatHold> customerHoldMap = new ConcurrentHashMap<>(SEAT_COUNT);

    public int numSeatsAvailable() {
        if (!customerHoldMap.isEmpty()) {
            for (SeatHold hold : customerHoldMap.values()) {
                for (Integer i : hold.getSeats()) {
                    tryToExpire(i);
                }
            }
        }

        return availableSeats.getCount();
    }

    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        SeatHold hold = new SeatHold();

        for (int i = 0; i < SEAT_COUNT; i++) {
            tryToExpire(i);

            boolean gotIt = seats.compareAndSet(i, OPEN, HELD + ":" + customerEmail);
            if (gotIt) {
                seatsTimestamp.set(i, new Date().getTime());
                availableSeats.decrement(1);
                hold.addSeat(i);
            }

            if (numSeats == hold.seatCount()) {
                customerHoldMap.put(hold.getId(), hold);
                hold.setCustomerEmail(customerEmail);
                return hold;
            }
        }

        // cleanup holds since we don't have all the requested seats
        for (Integer seat : hold.getSeats()) {
            if (seats.compareAndSet(seat, HELD + ":" + customerEmail, OPEN)) {
                seatsTimestamp.set(seat, Long.MAX_VALUE);
                availableSeats.increment(1);
            }
        }

        return null;
    }

    public String reserveSeats(int seatHoldId, String customerEmail) {
        SeatHold hold = customerHoldMap.get(seatHoldId);

        int reservedCount = 0;
        if (customerEmail.equals(hold.getCustomerEmail())) {
            for (Integer seatNumber : hold.getSeats()) {
                long heldTimestamp = seatsTimestamp.get(seatNumber);
                if (isExpired(seatNumber, new Date().getTime())) {
                    boolean done = seats.compareAndSet(seatNumber, HELD + ":" + customerEmail, OPEN);
                    if (done) {
                        availableSeats.increment(1);
                        seatsTimestamp.compareAndSet(seatNumber, heldTimestamp, Long.MAX_VALUE);
                    }
                } else {
                    if (seats.compareAndSet(seatNumber, HELD + ":" + customerEmail, RESERVED)) {
                        seatsTimestamp.compareAndSet(seatNumber, heldTimestamp, Long.MAX_VALUE);
                        availableSeats.increment(1);
                        reservedCount++;
                    }
                }
            }

            if (reservedCount == hold.seatCount()) {
                customerHoldMap.remove(hold.getId());
                return customerEmail + "-" + hold.getId();
            }
        }
        return null;
    }

    private boolean isExpired(int seatNumber, long time) {
        long heldTime = seatsTimestamp.get(seatNumber);
        if (heldTime == Long.MAX_VALUE) {
            return false;
        }

        return time > (heldTime + EXPIRATION_DELTA);
    }

    private void tryToExpire(int seatNumber) {
        long heldTimestamp = seatsTimestamp.get(seatNumber);

        if (isExpired(seatNumber, new Date().getTime())) {
            boolean done = seatsTimestamp.compareAndSet(seatNumber, heldTimestamp, Long.MAX_VALUE);
            if (done) {
                seats.set(seatNumber, OPEN);
                availableSeats.increment(1);
            }
        }
    }
}
