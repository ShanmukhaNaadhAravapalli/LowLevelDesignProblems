package splitwise;

import splitwise.entities.*;
import splitwise.strategy.SplitStrategy;

import java.util.*;
import java.util.stream.Collectors;

public class SplitWiseService {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Group> groups = new HashMap<>();
    private static class BillPugh{
        private static SplitWiseService splitWiseService = new SplitWiseService();
    }

    private SplitWiseService(){}

    public SplitWiseService getInstance(){
        return BillPugh.splitWiseService;
    }

    public User addUser(String name, String email) {
        User user = new User(name, email);
        users.put(user.getId(), user);
        return user;
    }

    public Group addGroup(String name, List<User> members) {
        Group group = new Group(name, members);
        groups.put(group.getId(), group);
        return group;
    }

    public synchronized  void createExpense(Expense.ExpenseBuilder builder){
        Expense expense = builder.build();
        User paidBy = expense.getPaidBy();
        Group group = expense.getGroup();
        BalanceSheet paidByBalanceSheet = paidBy.getBalanceSheetById( group.getId());
        for(Split split : expense.getSplits()){
            User participant = split.getUser();
            double amount = split.getAmount();
            BalanceSheet participantBalanceSheet = participant.getBalanceSheetById( group.getId());
            if(!paidBy.equals(participant)){
                paidByBalanceSheet.adjustBalance(participant, amount);
                participantBalanceSheet.adjustBalance(paidBy, -amount);
            }
        }
        System.out.println("Expense '" + expense.getDescription() + "' of amount " + expense.getAmount() + " created.");
    }

    public synchronized  void settleUp(String payerId, String payeeId, double amount, Group group){
        User payer = users.get(payerId);
        User payee = users.get(payeeId);
        System.out.println(payer.getName() + " is settling up " + amount + " with " + payee.getName());

        payee.getBalanceSheetById(group.getId()).adjustBalance(payer, -amount);
        payer.getBalanceSheetById(group.getId()).adjustBalance(payee, -amount);
    }

    public void showBalanceSheet(String userId, Group group) {
        User user = users.get(userId);
        user.getBalanceSheetById(group.getId()).showBalances();
    }

    public List<Transaction> simplifyGroupDebts(String groupId) throws IllegalArgumentException {
        Group group = groups.get(groupId);
        if(group == null)
             new IllegalArgumentException();
        Map<User, Double> netBalances = new HashMap<>();
        for(User member: group.getMembers()){
            double balance = 0;
            for( Map.Entry<User, Double> entry: member.getBalanceSheetById(groupId).getBalances().entrySet() ){
                if(group.getMembers().contains(entry.getKey()))
                    balance+= entry.getValue();
            }
            netBalances.put(member, balance);
        }
        List<Map.Entry<User, Double>> creditors = netBalances.entrySet().stream().filter( e -> e.getValue() > 0).collect(Collectors.toList());
        List<Map.Entry<User, Double>> debtors = netBalances.entrySet().stream().filter(e -> e.getValue() < 0).collect(Collectors.toList());
        creditors.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        debtors.sort(Map.Entry.comparingByValue());
        List<Transaction> transactions = new ArrayList<>();
        int i = 0; int j = 0;
        while(i < creditors.size() && j < debtors.size()){
            Map.Entry<User, Double> creditor = creditors.get(i);
            Map.Entry<User, Double> debtor = debtors.get(j);
            double amountToSettle = Math.min( creditor.getValue(), -debtor.getValue());
            transactions.add(new Transaction(debtor.getKey(), creditor.getKey(), amountToSettle));
            debtor.setValue( debtor.getValue() + amountToSettle);
            creditor.setValue(creditor.getValue() - amountToSettle);
            if(Math.abs(creditor.getValue()) < 0.01) i++;
            if(Math.abs(debtor.getValue()) > 0.01) j++;
        }
        return transactions;
    }

}

