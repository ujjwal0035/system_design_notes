import java.util.List;
import java.util.Map;

enum VehicleType{ TWO_WHEELER, THREE_WHEELER, FOUR_WHEELER, TRUCK };

class Vehicle {
    VehicleType type;
    String number;

    Vehicle(VehicleType type, String number) {
        this.type = type;
        this.number = number;
    }
}

class ParkingSpot {
    int id;
    VehicleType vehicleType;
    boolean isOccupied;
    boolean isVIP;
    boolean isEvChargingPoint;

    ParkingSpot(int id, VehicleType vehicleType, boolean isOccupied, boolean isVIP, boolean isEvChargingPoint) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.isOccupied = isOccupied;
        this.isVIP = isVIP;
        this.isEvChargingPoint = isEvChargingPoint;
    }
}

class Ticket{
    long entryTime;
    Vehicle vehicle;
    ParkingSpot parkingSpot;

    Ticket(Vehicle vehicle, ParkingSpot parkingSpot){
        this.entryTime = System.currentTimeMillis();
        this.vehicle   = vehicle;
        this.parkingSpot = parkingSpot;
    }
}


class ParkingManager{
    Map<Integer, List<ParkingSpot>> _FlOOR_WISE_SPOT; 

    ParkingManager(Map<Integer, List<ParkingSpot>> floorWiseSpot){
        this._FlOOR_WISE_SPOT = floorWiseSpot;
    }

    synchronized Ticket parkvehicle(Vehicle vehicle, boolean isVIP){ // assuming only gate for entry is avaible
        for (Integer floor : _FlOOR_WISE_SPOT.keySet()) {
            List<ParkingSpot> spots = _FlOOR_WISE_SPOT.get(floor);
            
            int leftIndex = 0; 
            int rightIndex = spots.size() - 1;

            while (leftIndex <= rightIndex) { // Standard two-pointer boundary
                ParkingSpot spot1 = spots.get(leftIndex);
                ParkingSpot spot2 = spots.get(rightIndex);

                // Check Left Spot
                if (isVIP && spot1.isVIP && !spot1.isOccupied && vehicle.type == spot1.vehicleType && spot1.isEvChargingPoint) {
                    spot1.isOccupied = true;
                    return new Ticket(vehicle, spot1);
                }
                
                // Check Right Spot (only if it's a different spot than leftIndex)
                if (leftIndex != rightIndex && isVIP && spot2.isVIP && !spot2.isOccupied && vehicle.type == spot2.vehicleType && spot2.isEvChargingPoint) {
                    spot2.isOccupied = true;
                    return new Ticket(vehicle, spot2);
                }

                // CRITICAL: Increment/Decrement indices to avoid infinite loop
                leftIndex++;
                rightIndex--;
            }
            for(ParkingSpot spot: spots){
                if(isVIP && spot.isVIP && !spot.isOccupied && vehicle.type == spot.vehicleType && spot.isEvChargingPoint){
                    spot.isOccupied = true;
                    return new Ticket(vehicle, spot);
                }
                else if(!spot.isOccupied && vehicle.type == spot.vehicleType && spot.isEvChargingPoint){
                    spot.isOccupied = true;
                    return new Ticket(vehicle, spot);
                }
            }
        }
        return null;
    }
}

class ParkingService{
    Map<VehicleType, Integer> _FIXED_PRICE_VEHICLE_WISE;

    ParkingService(Map<VehicleType, Integer> fixedPriceVehicleWise){
        this._FIXED_PRICE_VEHICLE_WISE = fixedPriceVehicleWise;
    }

    double unparkVehicle(Ticket ticket){
        Long currentTimeMillis = System.currentTimeMillis(); 
        Long totalTimeMillis = currentTimeMillis - ticket.entryTime;

        // vacent the parking spot
        ticket.parkingSpot.isOccupied = false;

        return calculatePrice(totalTimeMillis, ticket.vehicle);
    }

    // calculte the price
    double calculatePrice(Long totalTimeMillis, Vehicle vehicle){
        int amount = 0;
        Long totalTimeInHours = totalTimeMillis/(1000*60*60);
        if(totalTimeInHours>=1){
            amount += 20; totalTimeInHours -= 1;
        }
        amount += totalTimeInHours*10;
        return (double)amount;
    }
}