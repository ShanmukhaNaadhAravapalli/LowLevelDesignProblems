package ParkingLot;

import ParkingLot.entities.ParkingFloor;
import ParkingLot.entities.ParkingSpot;
import ParkingLot.entities.ParkingTicket;
import ParkingLot.strategy.fee.BaseFareStrategy;
import ParkingLot.strategy.fee.FareStrategy;
import ParkingLot.strategy.parking.NearestFloorStrategy;
import ParkingLot.strategy.parking.ParkingStrategy;
import ParkingLot.vehicles.Vehicle;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingLot {
    private final List<ParkingFloor> floors = new ArrayList<>();
    private FareStrategy fareStrategy;
    private ParkingStrategy parkingStrategy;
    private final Map<String, ParkingTicket> activeTickets;
    private ParkingLot(){
        this.fareStrategy = new BaseFareStrategy();
        this.parkingStrategy = new NearestFloorStrategy();
        this.activeTickets = new ConcurrentHashMap<>();
    }

    private static final class InstanceHolder {
        private static final ParkingLot instance = new ParkingLot();
    }

    public static ParkingLot getInstance(){
        return InstanceHolder.instance;
    }

    public void addFloor(ParkingFloor parkingFloor){
        this.floors.add(parkingFloor);
    }

    public void setFareStrategy(FareStrategy fareStrategy){
        this.fareStrategy = fareStrategy;
    }

    public void setParkingStrategy(ParkingStrategy parkingStrategy) {
        this.parkingStrategy = parkingStrategy;
    }

    public Optional<String> parkVehicle(Vehicle vehicle){
        Optional<ParkingSpot> availableSpot = parkingStrategy.findSpot(floors, vehicle);
        if(availableSpot.isPresent()){
            ParkingSpot spot = availableSpot.get();
            spot.parkVehicle(vehicle);
            ParkingTicket ticket = new ParkingTicket(UUID.randomUUID().toString(), spot, vehicle, LocalDateTime.now());
            activeTickets.put(ticket.getTicketId(), ticket);
            System.out.printf("%s parked at %s.Ticket: %s\n" , vehicle.getLicenseNumber(), spot.getSpotId(), ticket.getTicketId());
            return Optional.of(ticket.getTicketId());
        }
        return Optional.empty();
    }

    public Optional<Double> unparkVehicle(String ticketId, LocalDateTime endTime ){
        ParkingTicket ticket = activeTickets.remove(ticketId);
        if(ticket == null){
            System.out.println("Ticket Not found");
            return Optional.empty();
        }

        ticket.setEndTime(endTime);
        ticket.getSpot().unparkVehicle();
        Double parkingFee = fareStrategy.getPrice(ticket);
        return Optional.of(parkingFee);
    }

}
// improvements add new other two maps for Vehicle to ParkingSpot and ParkingSpot to Vehicle
