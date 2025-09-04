package ParkingLot;


import ParkingLot.entities.ParkingFloor;
import ParkingLot.entities.ParkingSpot;
import ParkingLot.vehicles.Vehicle;
import ParkingLot.vehicles.VehicleSize;

import java.time.LocalDateTime;
import java.util.Optional;

public class ParkingLotDemo {
    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.getInstance();
        ParkingFloor floor1 = new ParkingFloor();
        floor1.addSpot(new ParkingSpot( VehicleSize.SMALL));
        floor1.addSpot(new ParkingSpot( VehicleSize.MEDIUM));
        floor1.addSpot(new ParkingSpot( VehicleSize.LARGE));

        ParkingFloor floor2 = new ParkingFloor();
        floor2.addSpot(new ParkingSpot( VehicleSize.SMALL));
        floor2.addSpot(new ParkingSpot( VehicleSize.MEDIUM));
        floor2.addSpot(new ParkingSpot( VehicleSize.LARGE));
        floor2.addSpot(new ParkingSpot( VehicleSize.LARGE));
        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);

        Vehicle bike = new Vehicle("B-124",VehicleSize.SMALL );
        Vehicle car = new Vehicle("B-124",VehicleSize.MEDIUM );
        Vehicle truck = new Vehicle("B-124",VehicleSize.LARGE );

        Optional<String> bikeTicketOpt = parkingLot.parkVehicle(bike);

        Optional<String> carTicketOpt = parkingLot.parkVehicle(car);

        Optional<String> truckTicketOpt = parkingLot.parkVehicle(truck);

        System.out.println("\n--- Availability after parking ---");
        floor1.displayAvailability();
        floor2.displayAvailability();

        Vehicle car2 = new Vehicle("B-124",VehicleSize.MEDIUM );
        Optional<String> car2TicketOpt = parkingLot.parkVehicle(car2);

        Vehicle bike2 = new Vehicle("B-124",VehicleSize.SMALL );
        Optional<String> bike2TicketOpt = parkingLot.parkVehicle(bike2);
        LocalDateTime now = LocalDateTime.now();

        if (carTicketOpt.isPresent()) {
            Optional<Double> feeOpt = parkingLot.unparkVehicle(carTicketOpt.get(), now.plusHours(2));
            feeOpt.ifPresent(fee -> System.out.printf("Car C-456 unparked. Fee: $%.2f\n", fee));
        }

        if (bikeTicketOpt.isPresent()) {
            Optional<Double> feeOpt = parkingLot.unparkVehicle(bikeTicketOpt.get(), now.plusDays(2));
            feeOpt.ifPresent(fee -> System.out.printf("Car C-456 unparked. Fee: $%.2f\n", fee));
        }
        System.out.println("\n--- Availability after parking ---");
        floor1.displayAvailability();
        floor2.displayAvailability();

    }
}
