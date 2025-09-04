package ParkingLot.strategy.parking;

import ParkingLot.entities.ParkingFloor;
import ParkingLot.entities.ParkingSpot;
import ParkingLot.vehicles.Vehicle;

import java.util.List;
import java.util.Optional;

public interface ParkingStrategy {
    public Optional<ParkingSpot> findSpot(List<ParkingFloor> floors, Vehicle vehicle);
}
