package ParkingLot.entities;

import ParkingLot.vehicles.Vehicle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

public class ParkingTicket {
    private final String ticketId;
    private final ParkingSpot spot;
    private final Vehicle vehicle;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    public ParkingTicket(String ticketId, ParkingSpot spot, Vehicle vehicle, LocalDateTime startTime) {
        this.ticketId = ticketId;
        this.spot = spot;
        this.vehicle = vehicle;
        this.startTime = startTime;
    }

    public void setEndTime( LocalDateTime endTime){
        this.endTime = endTime;
    }

    public Duration calculateParkingDuration(){
        return Duration.between(startTime, endTime);
    }

    public String getTicketId() {
        return ticketId;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
