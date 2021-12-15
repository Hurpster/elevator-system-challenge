import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This class is the bridge that connects ElevatorRequest and Elevator
 */
public class ElevatorController implements Runnable{

    private boolean stopController;

    private static Map<Integer, Elevator> upMoving = new HashMap<Integer, Elevator>();
    private static Map<Integer, Elevator> downMoving = new HashMap<Integer, Elevator>();

    private static List<Elevator> elevatorList = new ArrayList<Elevator>(7);

    private static final ElevatorController instance = new ElevatorController();

    private ElevatorController() {
        if (instance != null) {
            throw new IllegalStateException("Already instantiated!");
        }
        setStopController(false);
        initializeElevators();
    }


    public static ElevatorController getInstance() {
        return instance;
    }


    public void setStopController(boolean stop) {
        this.stopController = stop;
    }

    public synchronized List<Elevator> getElevatorList() {
        return elevatorList;
    }


    public boolean isStopController() {
        return stopController;
    }


    public static void initializeElevators() {
        for (int i = 0; i < 7; i++) {
            Elevator elevator = new Elevator(i);
            Thread t = new Thread(elevator);
            t.start();

            elevatorList.add(elevator);
        }
    }


    private static ElevatorState getRequestedElevatorDirection(ElevatorRequest elevatorRequest) {

        ElevatorState elevatorState = null;
        int startFloor = elevatorRequest.getStartingFloor();
        int destinationFloor = elevatorRequest.getDestinationFloor();

        if (destinationFloor - startFloor > 0) {
            elevatorState = ElevatorState.UP;
        } else {
            elevatorState = ElevatorState.DOWN;
        }
        return elevatorState;
    }


    public synchronized Elevator selectElevator(ElevatorRequest elevatorRequest) {
        Elevator elevator = null;
        ElevatorState elevatorState = getRequestedElevatorDirection(elevatorRequest);
        int startFloor = elevatorRequest.getStartingFloor();
        int destinationFloor = elevatorRequest.getDestinationFloor();

        elevator = findElevator(elevatorState, startFloor, destinationFloor);
        // So the elevators can move again
        notifyAll();
        return elevator;
    }

    /**
     *
     * @param elevatorState UP,DOWN or IDLE
     * @param startFloor Floor number where the request comes from
     * @param destinationFloor Floor numbers where user wants to go
     * @return selected elevator
     */
    private static Elevator findElevator(ElevatorState elevatorState, int startFloor, int destinationFloor) {
        Elevator elevator = null;

        TreeMap<Integer, Integer> sortedKeyMap = new TreeMap<Integer, Integer>();

        if (elevatorState.equals(ElevatorState.UP)) {
            for (Map.Entry<Integer, Elevator> elevatorMap : upMoving.entrySet()) {
                Elevator lift = elevatorMap.getValue();
                Integer distance = startFloor - lift.getCurrentFloor();
                if (distance < 0 && lift.getElevatorState().equals(ElevatorState.UP)) {
                    continue;
                } else {
                    sortedKeyMap.put(Math.abs(distance), lift.getId());
                }
            }

            Integer selectedElevatorId = sortedKeyMap.firstEntry().getValue();
            elevator = upMoving.get(selectedElevatorId);

        } else if (elevatorState.equals(ElevatorState.DOWN)) {
            for (Map.Entry<Integer, Elevator> elevatorMap : downMoving.entrySet()) {
                Elevator lift = elevatorMap.getValue();
                Integer distance = lift.getCurrentFloor() - startFloor;
                if (distance < 0 && lift.getElevatorState().equals(ElevatorState.DOWN)) {
                    continue;
                } else {
                    sortedKeyMap.put(Math.abs(distance), lift.getId());
                }
            }
            Integer selectedElevatorId = sortedKeyMap.firstEntry().getValue();
            elevator = downMoving.get(selectedElevatorId);
        }

        // Elevator to stop/pass by relevant floors
        ElevatorRequest newRequest = new ElevatorRequest(elevator.getCurrentFloor(), startFloor);
        ElevatorState elevatorDirection = getRequestedElevatorDirection(newRequest);


        ElevatorRequest newerRequest = new ElevatorRequest(startFloor, destinationFloor);
        ElevatorState newElevatorDirection = getRequestedElevatorDirection(newerRequest);

        NavigableSet<Integer> floorSet = elevator.floorStopsMap.get(elevatorDirection);
        if (floorSet == null) {
            floorSet = new ConcurrentSkipListSet<Integer>();
        }

        floorSet.add(elevator.getCurrentFloor());
        floorSet.add(startFloor);
        elevator.floorStopsMap.put(elevatorDirection, floorSet);

        NavigableSet<Integer> floorSet2 = elevator.floorStopsMap.get(newElevatorDirection);
        if (floorSet2 == null) {
            floorSet2 = new ConcurrentSkipListSet<Integer>();
        }

        floorSet2.add(startFloor);
        floorSet2.add(destinationFloor);
        elevator.floorStopsMap.put(newElevatorDirection, floorSet2);

        return elevator;
    }
    // Updates elevatorState as soon as it changes direction
    public static synchronized void updateElevatorList(Elevator elevator){
        if(elevator.getElevatorState().equals(ElevatorState.UP)){
            upMoving.put(elevator.getId(), elevator);
            downMoving.remove(elevator.getId());
        }else if(elevator.getElevatorState().equals(ElevatorState.DOWN)){
            downMoving.put(elevator.getId(), elevator);
            upMoving.remove(elevator.getId());
        }else if(elevator.getElevatorState().equals(ElevatorState.IDLE)){
            upMoving.put(elevator.getId(), elevator);
            downMoving.put(elevator.getId(), elevator);
        }else if(elevator.getElevatorState().equals(ElevatorState.NONE)){
            upMoving.remove(elevator.getId());
            downMoving.remove(elevator.getId());
        }

    }

    @Override
    public void run() {
        stopController = false;
        while(true){
            try{
                Thread.sleep(100);
                if (stopController) {
                    break;
                }

            }catch(InterruptedException e){
                System.out.println(e.getStackTrace());
            }
        }
    }


}
