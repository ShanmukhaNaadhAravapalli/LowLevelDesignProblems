package splitwise.entities;

import splitwise.strategy.SplitStrategy;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Expense {
    private final String id;
    private final String description;
    private final double amount;
    private final Group group;
    private final User paidBy;
    private final List<Split> splits;
    private final LocalDateTime timestamp;
    private Expense(ExpenseBuilder builder){
        this.id = builder.id;
        this.description = builder.description;
        this.amount = builder.amount;
        this.paidBy = builder.paidBy;
        this.splits = builder.splitStrategy.calculateSplits(builder.amount, builder.paidBy, builder.participants, builder.splitValues);
        this.timestamp = LocalDateTime.now();
        this.group = builder.group;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public List<Split> getSplits() {
        return splits;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Group getGroup() {
        return group;
    }

    public static class ExpenseBuilder{
        private String id;
        private String description;
        private double amount;
        private User paidBy;
        private List<User> participants;
        private Group group;
        private SplitStrategy splitStrategy;
        private List<Double> splitValues;

        public ExpenseBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public ExpenseBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public ExpenseBuilder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public ExpenseBuilder setPaidBy(User paidBy) {
            this.paidBy = paidBy;
            return this;
        }

        public ExpenseBuilder setSplitStrategy( SplitStrategy splitStrategy){
            this.splitStrategy = splitStrategy;
            return this;
        }

        public ExpenseBuilder setParticipants(List<User> participants) {
            this.participants = participants;
            return this;
        }

        public ExpenseBuilder setSplitValues(List<Double> splitValues) {
            this.splitValues = splitValues;
            return this;
        }

        public Expense build() {
            return new Expense(this);
        }

        public ExpenseBuilder setGroup(Group group) {
            this.group = group;
            return this;
        }
    }
}
