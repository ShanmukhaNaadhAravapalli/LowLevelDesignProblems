package ParkingLot.entities;

import ParkingLot.vehicles.Vehicle;
import ParkingLot.vehicles.VehicleSize;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// we can add a display board later
public class ParkingFloor {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final Map<VehicleSize, List<ParkingSpot>> AvailableSpots;
    private final int floorId;
    public ParkingFloor(){
        this.floorId = counter.incrementAndGet();
        this.AvailableSpots = new ConcurrentHashMap<>();
    }
    public Optional<ParkingSpot> findAvailableSpot(Vehicle vehicle){
         VehicleSize vehicleSize = vehicle.getSize();
        // Start looking for the smallest spot that can fit the vehicle
         for(VehicleSize size : VehicleSize.values()){
             if(size.ordinal() >= vehicleSize.ordinal()){
                 List<ParkingSpot> spots = AvailableSpots.get(size);
                 for(ParkingSpot spot: spots){
                     if(spot.isAvailable())
                         return Optional.of(spot);
                 }
             }
         }
        return Optional.empty();
    }

    public void addSpot(ParkingSpot spot){
        AvailableSpots.computeIfAbsent(spot.getSpotType(), k -> new ArrayList<>()).add(spot);
    }

    public void displayAvailability(){
        System.out.printf("--- Floor %d Availability ---\n" , floorId);
        for(VehicleSize size: VehicleSize.values() ){
            System.out.printf(" %s spots: %d\n", size, AvailableSpots.get(size).stream().filter(ParkingSpot::isAvailable).count());
        }
    }
}
