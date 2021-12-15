import java.util.*;

/**
 * Objects of this class represents elevators individually
 */


public class Elevator implements Runnable{

    private int id;
    private ElevatorState elevatorState;
    private int currentFloor;
    private boolean active;

    // Set of floors through which the elevator moves
    private NavigableSet<Integer> floors;

    /* Map that serves requests that require an elevator
    to move both in UP and DOWN directions.
    ElevatorState stores UP/DOWN movements.
     */
    public Map<ElevatorState, NavigableSet<Integer>> floorStopsMap;


    public Elevator(int id) {
        this.id = id;
        setActive(true);
    }


    public int getId() {
        return id;
    }

    public ElevatorState getElevatorState() {
        return elevatorState;
    }

    public void setElevatorState(ElevatorState elevatorState) {
        this.elevatorState = elevatorState;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean state) {
        this.active= state;

        if(!state) {
            setElevatorState(ElevatorState.NONE);
            this.floors.clear();
        } else {
            setElevatorState(ElevatorState.IDLE);
            this.floorStopsMap = new LinkedHashMap<ElevatorState, NavigableSet<Integer>>();
            // Elevator ready for use
            ElevatorController.updateElevatorList(this);
        }

        setCurrentFloor(0);
    }

    public void move() {
        synchronized (ElevatorController.getInstance()) {
            Iterator<ElevatorState> iter = floorStopsMap.keySet().iterator();

            while (iter.hasNext()) {
                elevatorState = iter.next();
                // Get the floors through which the elevator will pass
                floors = floorStopsMap.get(elevatorState);
                iter.remove();

                Integer currentFlr = null;
                Integer nextFlr = null;

                // Start moving the elevator
                while (!floors.isEmpty()) {
                    if (elevatorState.equals(ElevatorState.UP)) {
                        currentFlr = floors.pollFirst();
                        nextFlr = floors.higher(currentFlr);
                    } else if (elevatorState.equals(ElevatorState.DOWN)) {
                        currentFlr = floors.pollLast();
                        nextFlr = floors.lower(currentFlr);
                    } else {
                        return;
                    }
                    setCurrentFloor(currentFlr);

                    if (nextFlr != null) {
                        // Helps picking up request during elevator movement
                        generateIntermediateFloors(currentFlr, nextFlr);
                    } else {
                        setElevatorState(ElevatorState.IDLE);
                        ElevatorController.updateElevatorList(this);
                    }
                    System.out.println("Elevator ID: "+ this.id + " | Current Floor: " + getCurrentFloor() + " | moving: " + getElevatorState());

                    try {
                        Thread.sleep(100);

                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            try{
				/* Wait until ElevatorController scans state of all elevators
				This helps us to serve intermediate request that might come
				during elevator movement
				 */
                ElevatorController.getInstance().wait();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }



    // Helps generate floorlists through which will elevator move(stop/pass)

    private void generateIntermediateFloors(int initial, int target){
        if(initial == target){
            return;
        }
        if(Math.abs(initial - target) == 1){
            return;
        }
        int n = 1;
        if(target - initial<0){
            // Elevator is moving down
            n = -1;
        }

        while(initial != target){
            initial += n;
            if(!floors.contains(initial)){
                floors.add(initial);
            }
        }
    }


    @Override
    public void run() {
        while(true){
            if(isActive()){
                move();
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }else{
                break;
            }
        }
    }
}
