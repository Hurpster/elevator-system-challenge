import java.util.Scanner;


public class ElevatorMain {

    private static ElevatorController elevatorController;
    private static Thread elevatorControllerThread;

    public static void main(String [ ] args){

        elevatorController = ElevatorController.getInstance();
        elevatorControllerThread = new Thread(elevatorController);
        elevatorControllerThread.start();
        int choice;

        while(true) {

            Scanner input = new Scanner(System.in);
            System.out.println("Enter choice (number): \n 1. Elevator status \n 2. Request elevator");
            choice = input.nextInt();

            if(choice == 1){
                input = new Scanner(System.in);
                System.out.println("Enter the elevator number (from 0 to 6): ");
                choice = input.nextInt();

                Elevator elevator = ElevatorController.getInstance().getElevatorList().get(choice);
                System.out.println("Elevator - " + elevator.getId() + " | Current floor - " + elevator.getCurrentFloor()
                        + " | Status - " + elevator.getElevatorState());
            }

            if(choice == 2) {
                input = new Scanner(System.in);
                System.out.println("Enter the floor where elevator is requested from (0 to 55): ");
                int requestFloor = input.nextInt();
                input = new Scanner(System.in);
                System.out.println("Enter the destination floor(0 to 55): ");
                int targetFloor = input.nextInt();

                ElevatorRequest elevatorRequest = new ElevatorRequest(requestFloor, targetFloor);
                Elevator elevator = elevatorRequest.submitRequest();
            }
        }
    }
}
