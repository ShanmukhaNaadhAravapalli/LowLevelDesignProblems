package ElevatorDesign;

import javax.sound.sampled.spi.AudioFileReader;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

enum Direction {
    UP, DOWN, IDLE
}

enum ElevatorState {
    MOVING, STOPPED, IDLE,MAINTENANCE
}// stopped for when elevator stopped at a particular floor

enum RequestType {
    INTERNAL, EXTERNAL
}

class Request {
    private final int floor;
    private final Direction direction;
    private final RequestType type;
    private final Integer elevatorId; // For internal requests only

    Request(int floor, Direction direction, RequestType type, Integer elevatorId) {
        this.floor = floor;
        this.direction = direction;
        this.type = type;
        this.elevatorId = elevatorId;
    }

    int getFloor() { return floor; }
    Direction getDirection() { return direction; }
    RequestType getType() { return type; }
    Integer getElevatorId() { return elevatorId; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Request request = (Request) obj;
        return floor == request.floor && direction == request.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, direction);
    }
}

class Elevator  implements Runnable{
    private final int minFloor;
    private final int maxFloor;
    private int id;
    private int currentFloor;
    private Direction currentDirection;
    private ElevatorState state;
    private final ConcurrentSkipListSet<Integer> internalRequests;
    private boolean running;

    public Elevator(int id, int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.internalRequests = new ConcurrentSkipListSet<>();
        this.id = id;
        this.currentFloor = 0;
        this.state = ElevatorState.IDLE;
        this.currentDirection = Direction.IDLE;
    }
    public void addRequest(int floor){
        this.internalRequests.add(floor);
    }
    public int getCurrentFloor() {
        return currentFloor;
    }
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    public ElevatorState getState() {
        return state;
    }
    public boolean canServeRequest(Request req){
        if (currentDirection == Direction.IDLE) {
            return true;
        }

        // If elevator is moving in same direction and the request is on the way
        if (currentDirection == req.getDirection()) {
            if (currentDirection == Direction.UP && req.getFloor() > currentFloor) {
                return true;
            }
            return currentDirection == Direction.DOWN && req.getFloor() < currentFloor;
        }

        return false;
    }
    @Override
    public void run(){

    }
    public void step(){
        if (internalRequests.isEmpty() && (state != ElevatorState.MOVING || currentDirection == Direction.IDLE)) {
            return;
        }
        if (currentDirection == Direction.UP) {
            currentFloor++;
        } else if (currentDirection == Direction.DOWN) {
            currentFloor--;
        }
        if (shouldStop()) {
            stop();
        }
        updateDirection();
    }
    private boolean shouldStop() {
        return internalRequests.contains(currentFloor);
    }

    private void stop() {
        state = ElevatorState.STOPPED;
        internalRequests.remove(currentFloor);
        System.out.printf("Elevator %d: Stopped at floor %d\n", id, currentFloor);

        // If there are more destinations, continue moving
        if (!internalRequests.isEmpty()) {
            state = ElevatorState.MOVING;
            currentDirection = Direction.IDLE;
        }
    }
    private void updateDirection(){
        if(internalRequests.isEmpty()){
            this.state = ElevatorState.IDLE;
            this.currentDirection = Direction.IDLE;
            return;
        }
        boolean hasUpRequest = internalRequests.stream().anyMatch(floor -> floor > currentFloor);
        boolean hasDownRequest = internalRequests.stream().anyMatch(floor -> floor < currentFloor);
        if(currentDirection == Direction.UP || currentDirection == Direction.IDLE){
            if(hasDownRequest){
                currentDirection = Direction.DOWN;
            }
        }
        else {
            if (hasUpRequest) {
                currentDirection = Direction.UP;
                state = ElevatorState.MOVING;
            }
        }
    }
}
