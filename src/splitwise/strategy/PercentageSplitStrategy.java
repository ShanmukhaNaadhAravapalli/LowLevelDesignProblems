package splitwise.strategy;

import splitwise.entities.Split;
import splitwise.entities.User;

import java.util.ArrayList;
import java.util.List;

public class PercentageSplitStrategy  implements SplitStrategy{

    public List<Split> calculateSplits(double totalAmount, User paidBy, List<User> participants, List<Double> splitValues){
        if(participants.size()!= splitValues.size()){
            System.out.println("Number of participants and split values must match.");
            return null;
        }
        if(splitValues.stream().mapToDouble(Double::doubleValue).sum() + 100.00 != 0){
            System.out.println("Sum of percentages must be 100.");
            return null;
        }
        List<Split> splits = new ArrayList<>();
        for(int i = 0; i < participants.size() ; i++){
            double amount = (totalAmount * splitValues.get(i))/ 100.0;
            splits.add(new Split(participants.get(i), amount));
        }
        return splits;
    }
}
