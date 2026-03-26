package RestaurantBookingSystem;

import java.awt.event.WindowStateListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class Table {
    final int id;
    final int capacity;
    boolean isFree ;

    public Table(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.isFree = true;
    }
    public void free(){
        this.isFree = true;
    }
    public void reserve(){
        isFree = false;
    }



    public int getId() {
        return id;
    }

    public boolean isFree() {
        return isFree;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Table#" + id + "(cap=" + capacity + ")";
    }
}

class WaitListEntry {
    private final String name;
    private int partySize;
    private long arrivalTime;

    public WaitListEntry(String name, int partySize) {
        this.name = name;
        this.partySize = partySize;
        this.arrivalTime = System.currentTimeMillis();;
    }

    public String getName() {
        return name;
    }

    public int getPartySize() {
        return partySize;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }
}

class TableManager {
    private static TableManager instance;
    private final Map<Integer, Table> tablesbyId = new HashMap<>();
    private final AtomicInteger tableSeq = new AtomicInteger(0);

    private TableManager() {

    }
    public static TableManager getInstance(){
        if(instance == null)
            instance = new TableManager();
        return instance;
    }

    public void addTable(Table table) {
        tablesbyId.put(table.getId(), table);
    }

    public void createTable(int capacity) {
        int id = tableSeq.incrementAndGet();
        tablesbyId.put(id, new Table(id, capacity));
    }


    public boolean isFree(int tableid) {
        if (tablesbyId.get(tableid) == null)
            return false;
        return tablesbyId.get(tableid).isFree();
    }

    public Table getTable(int tableId) {
        return tablesbyId.get(tableId);
    }

    public void freeTable(int tableId) {
        Table table = tablesbyId.get(tableId);
        if (table != null) {
            table.free();
        }

    }

    public List<Table> getAvailableTables(int size){
        return tablesbyId.values().stream().filter(Table::isFree).filter( t -> t.getCapacity() >= size).sorted(Comparator.comparing(Table::getCapacity)).collect(Collectors.toList());
    }
}

class WaitListManager {
    private static WaitListManager instance;
    private final Queue<WaitListEntry> waitlist = new LinkedList<>();

    public static WaitListManager getInstance(){
        if(instance == null)
            instance = new WaitListManager();
        return instance;
    }
    public void addGroup(String name, int partySize){
        waitlist.offer(new WaitListEntry(name, partySize));
        System.out.println("Added to waitlist: " + name +
                " (Position: " + waitlist.size() +
                ", Est. wait: " + estimateWait() + " min)");
    }
    public Optional<WaitListEntry> findGroupForTable(int tableCapacity){
        return waitlist.stream().filter(entry -> entry.getPartySize() <= tableCapacity).findFirst();
    }

    public void removeGroup(WaitListEntry entry){
        waitlist.remove(entry);
    }
    private int estimateWait() {
        return waitlist.size() * 15; // 15 min per party
    }

}
class SeatingService {
    private final TableManager tableManager;
    private final WaitListManager waitListManager;

    public SeatingService(TableManager tableManager, WaitListManager waitListManager) {
        this.tableManager = tableManager;
        this.waitListManager = waitListManager;
    }


    public synchronized void onTableFreed(int tableId){
        Table table = tableManager.getTable(tableId);
        if(table == null || !table.isFree())
            return;
        Optional<WaitListEntry> groupOpt = waitListManager.findGroupForTable(table.getCapacity());
        if(groupOpt.isEmpty()){
            System.out.println("No waitlist group fits " + table.toString());
            return;
        }
        WaitListEntry group = groupOpt.get();
        table.reserve();
        waitListManager.removeGroup(group);
        System.out.println(
                "Seated " + group.getName() +
                        " (party=" + group.getPartySize() +
                        ") at " + table
        );
    }

    public TableManager getTableManager() {
        return tableManager;
    }

    public WaitListManager getWaitListManager() {
        return waitListManager;
    }
}

class RestaurantSystem {

    private final WaitListManager waitlistManager;
    private final SeatingService seatingService;
    private final TableManager tableManager;

    public RestaurantSystem() {
        this.tableManager = TableManager.getInstance();
        this.waitlistManager = WaitListManager.getInstance();
        this.seatingService =
                new SeatingService(tableManager, waitlistManager);
    }

    // ⭐ AddGroup()
    public void addGroup(String name, int partySize) {
        // Check if table available NOW
        List<Table> available = tableManager.getAvailableTables(partySize);

        if (!available.isEmpty()) {
            Table table = available.get(0);
            seatImmediately(name, partySize, table);
            System.out.println("✓ Seated immediately: " + name);
        } else {
            waitlistManager.addGroup(name, partySize);
        }
    }

    private void seatImmediately(String name, int size, Table table) {
        table.reserve();
        // Track who's seated (need Booking entity)
        System.out.println("✓ " + name + " seated at " + table);
    }

    // add Table
    public void addTable(int capacity){
        tableManager.createTable(capacity);
    }

    // ⭐ Called by front desk when table is cleared
    public void tableFreed(int tableId) {
        tableManager.freeTable(tableId);
        seatingService.onTableFreed(tableId);
    }
}

public class RestaurantBookingSystemDemo {
    public static void main(String[] args) {
    }
}
