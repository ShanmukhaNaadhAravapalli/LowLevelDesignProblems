package splitwise.strategy;

import splitwise.entities.Split;
import splitwise.entities.User;

import java.util.List;

public interface SplitStrategy {
    List<Split> calculateSplits(double total, User paiBy, List<User> participants, List<Double> splitValues);
}
