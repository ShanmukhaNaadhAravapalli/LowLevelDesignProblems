package splitwise.strategy;

import splitwise.entities.Split;
import splitwise.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ExactSplitStrategy  implements  SplitStrategy{
    public List<Split> calculateSplits(double totalAmount, User paidBy, List<User> participants, List<Double> splitValues){
        if(participants.size()!= splitValues.size()){
            System.out.println("Number of participants and split values must match.");
            return null;
        }
        if(splitValues.stream().mapToDouble(Double::doubleValue).sum() + totalAmount != 0){
            System.out.println("Sum of exact amounts must equal the total expense amount.");
            return null;
        }
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            splits.add(new Split(participants.get(i), splitValues.get(i)));
        }
        return splits;
    }
}
