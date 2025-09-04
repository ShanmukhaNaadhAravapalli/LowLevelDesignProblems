package ParkingLot.strategy.fee;


import ParkingLot.entities.ParkingTicket;

import java.time.Duration;

public class TieredFareStrategy implements FareStrategy {
    private static final double FIRST_HOUR_RATE = 5.0;
    private static final double ADDITIONAL_HOUR_RATE = 8.0;

    @Override
    public double getPrice(ParkingTicket ticket) {
        Duration duration = ticket.calculateParkingDuration();
        long totalHours = duration.toHours();
        long remainingMinutes = duration.toMinutes() % 60;
        if (remainingMinutes > 0) totalHours++;

        if (totalHours <= 1) {
            return FIRST_HOUR_RATE;
        } else {
            return FIRST_HOUR_RATE + (totalHours - 1) * ADDITIONAL_HOUR_RATE;
        }
    }
}

