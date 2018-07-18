package ticketservice;

import java.util.concurrent.atomic.AtomicInteger;

public class IntegerCounter {
    private AtomicInteger counter;

    public IntegerCounter(int capacity) {
        counter = new AtomicInteger(capacity);
    }

    int getCount() {
        return counter.get();
    }

    void increment(int num) {
        while(true) {
            int existingValue = getCount();
            int newValue = existingValue + num;
            if(counter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }

    void decrement(int num) {
        while(true) {
            int existingValue = getCount();
            int newValue = existingValue - num;
            if(counter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }
}