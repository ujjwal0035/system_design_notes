import java.util.List;
import java.util.Map;

enum VehicleType{ TWO_WHEELER, THREE_WHEELER, FOUR_WHEELER, TRUCK };

class Vehicle {
    VehicleType type;
    String number;
    boolean isVIP;
    boolean isEvCar;

    Vehicle(VehicleType type, String number) {
        this.type = type;
        this.number = number;
        this.isVIP = isVIP;
        this.isEvCar = isEvCar;
    }
}

class ParkingSpot {
    int id;
    VehicleType vehicleType;
    boolean isOccupied;
    int distanceFromEntryGate;
    boolean isVIP;
    boolean isEvChargingPoint;

    ParkingSpot(int id, VehicleType vehicleType, boolean isOccupied, int distanceFromEntryGate, boolean isVIP, boolean isEvChargingPoint) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.isOccupied = isOccupied;
        this.distanceFromEntryGate = distanceFromEntryGate;
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


// this class manage the parking at right spot
class ParkingManager{
    Map<Integer, List<ParkingSpot>> _FlOOR_WISE_SPOT;
    int minFloor = 0; int maxFloor = 0; // this to identify the how many floor of parking is there -ve and +ve

    ParkingManager(Map<Integer, List<ParkingSpot>> floorWiseSpot, int minFloor, int maxFloor){
        this._FlOOR_WISE_SPOT = floorWiseSpot;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    boolean isValidSpot(Vehicle vehicle, ParkingSpot spot){
        if(spot.isOccupied) return false;
        if(spot.vehicleType != vehicle.type) return false;
        if(vehicle.isVIP != spot.isVIP) return false;
        if(vehicle.isEvCar != spot.isEvChargingPoint) return false;
        return true;
    }

    Ticket handleParkingVechile(int floor, Vehicle vehicle){
        List<ParkingSpot> spots = this._FlOOR_WISE_SPOT.get(floor);
        spots.sort((a,b) -> a.distanceFromEntryGate - b.distanceFromEntryGate); // sorting the spot based on the distance from gate

        for(ParkingSpot spot: spots){
            if(isValidSpot(vehicle, spot)){
                return new Ticket(vehicle, spot);
            }
        }

        return null;
    } 

    synchronized Ticket parkvehicle(Vehicle vehicle, boolean isVIP){ // assuming only gate for entry is avaible
        // check if which floor is nearest to the gate assuming gate is at the ground floor
        int floorGround = 0; int floorUp = 0; 
        while(floorGround >= minFloor || floorUp <= maxFloor){
            if(floorGround >= minFloor){
                Ticket ticket = handleParkingVechile(floorGround, vehicle);
                if(ticket != null) return ticket;
                floorGround--;
            }
            if(floorUp <= maxFloor){
                Ticket ticket = handleParkingVechile(floorUp, vehicle);
                if(ticket != null) return ticket;
                floorUp++;
            }
        }
        return null;
    }
}


// this is manage for exit service
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