package splitwise.entities;

import java.util.List;
import java.util.UUID;

public class Group {
    private final String id;
    private final String name;
    private List<User> members;

    public Group(String name, List<User> members) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.members = members;
        addNewBalanceSheet();
    }

    public void addNewBalanceSheet(){
        for(User user: members){
            user.addNewBalanceSheet(id);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return members;
    }

    public void add(User user){
        members.add(user);
    }


}
