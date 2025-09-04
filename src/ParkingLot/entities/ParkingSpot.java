package ParkingLot.entities;

import ParkingLot.vehicles.Vehicle;
import ParkingLot.vehicles.VehicleSize;

import java.util.concurrent.atomic.AtomicInteger;

// another way is to make below class abstract and create classes for each vehicleType
public class ParkingSpot {
    private final int spotId;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private boolean isAvailable;
    private Vehicle vehicle;
    private final VehicleSize spotType;
    public ParkingSpot( VehicleSize type){
        this.spotId = counter.incrementAndGet();
        this.isAvailable = true;
        this.vehicle = null;
        this.spotType = type;
    }
    public void parkVehicle(Vehicle vehicle){
        this.vehicle = vehicle;
        this.isAvailable = false;
    }
    public void unparkVehicle(){
        this.vehicle = null;
        this.isAvailable = true;
    }
    public boolean isAvailable(){
        return isAvailable;
    }
    public Vehicle getVehicle(){
        return vehicle;
    }
    public VehicleSize getSpotType() {
        return spotType;
    }

    public int getSpotId() {
        return spotId;
    }
}
