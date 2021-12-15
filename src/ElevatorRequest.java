/**
 * Represents a request for the user to use the elevator
 */
public class ElevatorRequest {
    private int startingFloor;
    private int destinationFloor;


    public ElevatorRequest(int startingFloor, int destinationFloor) {
        this.startingFloor = startingFloor;
        this.destinationFloor = destinationFloor;
    }


    public int getStartingFloor() {
        return startingFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    // Submit the request to the ElevatorControl
    // to select best elevator for this request
    public Elevator submitRequest() {
        return ElevatorController.getInstance().selectElevator(this);
    }

}
