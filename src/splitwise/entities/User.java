package splitwise.entities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String id;
    private final String name;
    private final String email;
    private final Map<String, BalanceSheet> balanceSheet;

    public User(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.balanceSheet = new ConcurrentHashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, BalanceSheet> getBalanceSheet() {
        return balanceSheet;
    }

    public BalanceSheet getBalanceSheetById(String groupId) {
        return balanceSheet.get(groupId);
    }

    public void addNewBalanceSheet(String id){
        balanceSheet.put(id, new BalanceSheet(this, id));
    }
}
