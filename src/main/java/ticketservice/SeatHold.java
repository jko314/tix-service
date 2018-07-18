package ticketservice;

import java.util.ArrayList;
import java.util.List;

public class SeatHold {
    public static Long EXPIRATION_DELTA = 300000L; //5 min

    private int id;
    private String customerEmail;
    private List<Integer> seats = new ArrayList<>();
    private long expirationTimeStamp;
    private static IntegerCounter idCounter = new IntegerCounter(0);

    public SeatHold() {
        id = idCounter.getCount();
        idCounter.increment(1);
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public long getExpirationTimeStamp() {
        return expirationTimeStamp;
    }

    public void setExpirationTimeStamp(long expirationTimeStamp) {
        this.expirationTimeStamp = expirationTimeStamp;
    }

    public boolean isExpired(long time) {
        return time > (expirationTimeStamp + EXPIRATION_DELTA);
    }

    public void addSeat(Integer seat) {
        seats.add(seat);
    }

    public List<Integer> getSeats() {
        return seats;
    }

    public int seatCount() {
        return seats.size();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
