package ParkingLot.strategy.fee;

import ParkingLot.entities.ParkingTicket;

import java.time.Duration;

public class BaseFareStrategy implements FareStrategy{
    private static final double RATE_PER_HOUR = 10.0;
    public double getPrice(ParkingTicket ticket){
        Duration duration = ticket.calculateParkingDuration();
        long totalHours = duration.toHours();
        long remainingMinutes = duration.toMinutes() % 60;
        if(remainingMinutes > 0)
            totalHours+= 1;
        return totalHours * RATE_PER_HOUR;
    }
}
