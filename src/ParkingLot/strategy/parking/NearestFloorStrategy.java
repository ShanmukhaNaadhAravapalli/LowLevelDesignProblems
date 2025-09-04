package ParkingLot.strategy.parking;

import ParkingLot.entities.ParkingFloor;
import ParkingLot.entities.ParkingSpot;
import ParkingLot.vehicles.Vehicle;

import java.util.List;
import java.util.Optional;

public class NearestFloorStrategy implements ParkingStrategy{
    public Optional<ParkingSpot> findSpot(List<ParkingFloor> floors, Vehicle vehicle){
        for(ParkingFloor floor: floors){
            Optional<ParkingSpot> freeSpot = floor.findAvailableSpot(vehicle);
            if(freeSpot.isPresent())
                return  freeSpot;
        }
        return Optional.empty();
    }
}
