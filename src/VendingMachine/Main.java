package VendingMachine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

enum ItemType {
    COKE,
    PEPSI,
    JUICE,
    SODA;
}
enum Coin {
    PENNY(1), NICKEL(5), DIME(10), QUARTER(25);

    private int value;

    Coin(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

enum PaymentType {
    COIN
}
class Item{
    private int price;
    private ItemType type;
    private String name;

    public Item(int price, ItemType type, String name) {
        this.price = price;
        this.type = type;
        this.name = name;
    }

    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

class ItemShelf{
    private int code;
    private int quantity;
    private Item item;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(int quantity) {
        this.quantity -= quantity;
    }

    public boolean isSoldOut(){
        return quantity == 0;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}

class Inventory{
    ItemShelf itemShelves[];
    public Inventory(int count){
        itemShelves = new ItemShelf[count];
        initialEmptyInventory();
    }
    public void initialEmptyInventory(){
        int startCode = 101;
        for (int i = 0; i < itemShelves.length; i++) {
            ItemShelf space = new ItemShelf();
            space.setCode(startCode);
            space.setQuantity(0);
            itemShelves[i]= space;
            startCode++;
        }
    }

    public ItemShelf[] getInventory() {
        return itemShelves;
    }

    public void setInventory(ItemShelf[] itemShelves) {
        this.itemShelves  = itemShelves ;
    }

    public void addItem(Item item, int codeNumber) throws Exception {

        for (ItemShelf itemShelf : itemShelves) {
            if (itemShelf.getCode() == codeNumber) {
                if (itemShelf.isSoldOut()) {
                    itemShelf.setItem(item);
                    itemShelf.setQuantity(0);
                } else {
                    throw new Exception("already item is present, you can not add item here");
                }
            }
        }
    }

    public Item getItem(int codeNumber)  {

        for (ItemShelf itemShelf : itemShelves) {
            if (itemShelf.getCode() == codeNumber) {
                if (itemShelf.isSoldOut()) {
                    System.out.println("item already sold out");
                } else {

                    return itemShelf.getItem();
                }
            }
        }
        return null;
    }

    public void addCount(int codeNumber, int quantity) throws Exception {
        for (ItemShelf itemShelf : itemShelves) {
            if (itemShelf.getCode() == codeNumber) {
                if (itemShelf.isSoldOut()) {
                    throw new Exception("item already sold out");
                } else {
                    itemShelf.addQuantity(quantity);
                }
            }
        }
        throw new Exception("Invalid Code");
    }

    public void reduceCount(int codeNumber, int quantity) throws Exception {
        for (ItemShelf itemShelf : itemShelves) {
            if (itemShelf.getCode() == codeNumber) {
                if (itemShelf.isSoldOut()) {
                    throw new Exception("item already sold out");
                } else {
                    itemShelf.removeQuantity(quantity);
                }
            }
        }
        throw new Exception("Invalid Code");
    }

}

interface VendingMachineState {
    void insertCoin(VendingMachineContext context, Coin coin);
    void startProductSelection(VendingMachineContext context);
    void selectProduct(VendingMachineContext context, int shelfCode, int quantity);
    void cancelPayment(VendingMachineContext context);
    void dispenseProduct(VendingMachineContext context);
}
class IdleState implements VendingMachineState {
    public IdleState() {
        System.out.println("Vending machine is now in Idle State");
    }
    public void insertCoin(VendingMachineContext context, Coin coin){
        context.insertCoin(coin);
    }
    public void startProductSelection(VendingMachineContext context){
        context.setCurrentState(new HasMoneyState());
    }
    public void selectProduct(VendingMachineContext context, int shelfCode, int quantity){
        System.out.println("product cannot be selected in Idle State. Please select start Product selection after inserting money");
    }
    public void cancelPayment(VendingMachineContext context){
        System.out.println("returning money");
        context.resetCurrentState();
    }

    public void dispenseProduct(VendingMachineContext context){
        System.out.println("product can not be dispensed in Idle state");
    }
}

class HasMoneyState implements VendingMachineState {
    public HasMoneyState() {
        System.out.println("Vending machine is now in HasMoney State");
    }
    public void insertCoin(VendingMachineContext context, Coin coin){
        System.out.println("You have already inserted money");
    }
    public void startProductSelection(VendingMachineContext context){
        System.out.println("You are already in HasMoneyState state");
    }
    public void selectProduct(VendingMachineContext context, int shelfCode, int quantity){
        if(context.getInventory().getItem(shelfCode) == null){
            return;
        }
        context.setSelectedItemCode(shelfCode);
        context.setQuantity(quantity);
        context.setCurrentState(new DispensingState());
    }
    public void cancelPayment(VendingMachineContext context){
        System.out.println("returning money");
        context.resetCurrentState();
    }

    public void dispenseProduct(VendingMachineContext context){
        System.out.println("product can not be dispensed in HasMoneyState state");
    }
}

class DispensingState implements VendingMachineState{
    public DispensingState() {
        System.out.println("Vending machine is now in HasMoney State");
    }
    public void insertCoin(VendingMachineContext context, Coin coin){
        System.out.println("You have already inserted money");
    }
    public void startProductSelection(VendingMachineContext context){
        System.out.println("You have already selected product");
    }
    public void selectProduct(VendingMachineContext context, int shelfCode, int quantity){
        System.out.println("You have already selected product");
    }
    public void cancelPayment(VendingMachineContext context){
        System.out.println("returning money");
        context.resetCurrentState();
    }

    public void dispenseProduct(VendingMachineContext context){
        System.out.println("Product has been dispensed");
        int balance  = context.getBalance();
        int amountTobePaid = context.getTotalAmount();
        if(balance >= amountTobePaid){
            System.out.println("dispensing change" + (balance - amountTobePaid));
            try{
                context.getInventory().addCount(context.getSelectedItemCode(), -context.getQuantity());
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("Product has been dispensed");
            context.resetCurrentState();
        }
        else{
            System.out.println("please add more money need"+ (amountTobePaid - balance));
            context.setCurrentState(new IdleState());
        }

    }
}
class VendingMachineContext {
    private VendingMachineState currentState;
    private Inventory inventory;
    private List<Coin> coinList;
    private Integer selectedItemCode;
    private int quantity;
    public VendingMachineContext(int count){
        inventory = new Inventory(10);
        currentState = new IdleState();
        coinList = new ArrayList<>();
        selectedItemCode = -1;
        System.out.println("Initialized: " + currentState.getClass());
    }
    public void insertCoin(Coin coin){
        this.currentState.insertCoin(this, coin);
    }

    public void startProductSelection(){
        this.currentState.startProductSelection(this);
    }

    public void selectProduct(int shelfCode, int quantity){
        currentState.selectProduct(this, shelfCode, quantity);
    }
    public void cancelPayment(VendingMachineContext context){
        context.cancelPayment(this);
    }

    public VendingMachineState getCurrentState(){
        return currentState;
    }
    public void dispenseProduct(){
        currentState.dispenseProduct(this);
    }

    public void setCurrentState(VendingMachineState newState){
        currentState = newState;
    }

    public void resetCurrentState(){
        this.selectedItemCode = -1;
        this.coinList.clear();
        currentState = new IdleState();;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Coin> getCoinList() {
        return coinList;
    }

    public int getSelectedItemCode() {
        return selectedItemCode;
    }

    public void addCoins(Coin coin) {
        this.coinList.add(coin);
    }

    public void setSelectedItemCode(int selectedItemCode) {
        this.selectedItemCode = selectedItemCode;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getBalance() {
        int balance = 0;
        for (Coin coin : coinList) {
            balance += coin.getValue(); // Sum up the coin values
        }
        return balance;
    }

    public int getTotalAmount(){
        return inventory.getItem(selectedItemCode).getPrice() * quantity;
    }

    public void resetBalance() {
        coinList.clear();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


}




public class Main {
}
