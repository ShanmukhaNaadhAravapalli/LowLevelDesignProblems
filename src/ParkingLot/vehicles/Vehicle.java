package ParkingLot.vehicles;

public class Vehicle {
    private final String license_number;
    private final VehicleSize size;
    public Vehicle( String license, VehicleSize size) {
        this.license_number = license;
        this.size = size;
    }

    public VehicleSize getSize() {
        return size;
    }

    public String getLicenseNumber() {
        return license_number;
    }
}

