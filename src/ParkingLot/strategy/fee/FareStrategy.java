package ParkingLot.strategy.fee;


import ParkingLot.entities.ParkingTicket;

public interface FareStrategy {
    public double getPrice(ParkingTicket ticket) ;
}
