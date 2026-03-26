package FoodDeliverySystem;

import java.beans.Customizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

enum DeliveryPartnerStatus {
    AVAILABLE, BUSY, OFFLINE
}

enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

enum RestaurantStatus{
    OPEN,
    CLOSED,
    BUSY
}
class MenuItem {
    private final String id;
    private double price;
    private final String name;
    private boolean isAvailable;
    // description, veg/non-veg, CuisineType(india, chineese, italian, mexican)
    public MenuItem(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.isAvailable = true;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}

class Menu {
    private Map<String, MenuItem> items = new ConcurrentHashMap<>();


    public void addItem(MenuItem item) {
        items.put(item.getId(), item);
    }

    public MenuItem getItem(String id) { return items.get(id); }

    public Map<String, MenuItem> getItems() { return items; }
}
class Address {
    private final String street;
    private final String city;
    private final String zipCode;
    private final double latitude;
    private final double longitude;
    public Address(double latitude, double longitude, String address,
                    String city, String zipCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.street = address;
        this.city = city;
        this.zipCode = zipCode;
    }
    public double distanceTo(Address other) {
        // Haversine formula
        double R = 6371; // Earth radius in km
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

class User {
    private final String userId;
    private final String name;
    private final String email;
    private final String number;

    public User(String userId, String name, String email, String number) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.number = number;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }
}

class Customer extends  User {
    private final List<Address> savedAddresses;
    private final List<Order> orderHistory;
    private Address defaultAddress ;

    public Customer(String userId, String name, String email, String number, Address address) {
        super(userId, name, email, number);
        this.savedAddresses = new ArrayList<>();
        savedAddresses.add(address);
        this.orderHistory = new ArrayList<>();
        this.defaultAddress = address;

    }

    public void addAddress(Address address) {
        savedAddresses.add(address);
        if (defaultAddress == null) {
            defaultAddress = address;
        }
    }

    public void setDefaultAddress(Address address) {
        this.defaultAddress = address;
    }

    public void addOrderToHistory(Order order) {
        orderHistory.add(order);
    }

    public Address getDefaultAddress() {
        return defaultAddress;
    }

    public List<Order> getOrderHistory() {
        return new ArrayList<>(orderHistory);
    }
}


class DeliveryPartner extends  User{
    private AtomicBoolean isAvailable;
    private Address currentLocation;

    public DeliveryPartner(String userId, String name, String email, String number, Address currentLocation) {
        super(userId, name, email, number);
        this.currentLocation = currentLocation;
        this.setAvailable(true);
    }


    public AtomicBoolean getIsAvailable() {
        return isAvailable;
    }

    public Address getCurrentLocation() {
        return currentLocation;
    }


    public void setAvailable(boolean available) {
        this.isAvailable.set(available);
    }


    // can add history of deliveries, rating, DeliveryPartnerStatus instead of isAvailable, currentOrderId
}

class OrderItem {
    private int quantity;
    private MenuItem Item;

    public OrderItem(int quantity, MenuItem item) {
        this.quantity = quantity;
        Item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public MenuItem getItem() {
        return Item;
    }
    // special instructions
}
class Order {
    private final String orderId;
    private final Restaurant restaurant;
    private final Customer customer;
    private final Address address;
    private OrderStatus status;
    private DeliveryPartner deliveryPartner;
    private List<OrderItem> orderItems;
    private double totalAmount;
    // deliveryFee, tax, PaymentMethod, PaymentStatus
    public Order(String orderId, Restaurant restaurant, Customer customer, Address address) {
        this.orderId = orderId;
        this.restaurant = restaurant;
        this.customer = customer;
        this.address = address;
    }

    public String getOrderId() {
        return orderId;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Address getAddress() {
        return address;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Order setStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public Order setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
        return this;
    }

    // public void printOrderSummary()
}
class Cart {
    private final User user;
    private final Restaurant restaurant;
    private final Map<MenuItem, Integer> items = new ConcurrentHashMap<>();

    public Cart(User user, Restaurant restaurant) {
        this.user = user;
        this.restaurant = restaurant;
    }

    public void addItem(MenuItem item , int qty){
        items.put(item, items.getOrDefault(item, 0) + qty);
    }
    public Map<MenuItem, Integer> getItems() {
        return Map.copyOf(items);
    }
    public double totalAmount(){
        return  items.entrySet().stream().mapToDouble(e -> e.getKey().getPrice() * e.getValue()).sum();
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
}
class Restaurant {
    private final String id;
    private final String name;
    private final Address address;
    private Menu menu;

    public Restaurant(Address address, String id, String name) {
        this.address = address;
        this.id = id;
        this.name = name;
        this.menu = new Menu();
    }

    // restaurant status, rating, deliveryRadius, totalOrders
    public String getId() {
        return id;
    }

    public void addToMenu(MenuItem item) { this.menu.addItem(item); }

    public Address getAddress() {
        return address;
    }

    public Menu getMenu() {
        return menu;
    }
}


class CartService {
    private final Map<String, Cart> activeCarts = new ConcurrentHashMap<>();
    private static CartService instance;
    private CartService(){}
    public static CartService getInstance(){
        if(instance == null)
            instance = new CartService();
        return instance;
    }
    public Cart getOrCreateCart(User user, Restaurant restaurant){
        return activeCarts.compute(user.getUserId(), (uid, existingCart) ->{
            if(existingCart!= null && !existingCart.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalStateException(
                        "User already has an active cart with another restaurant");
            }
            return existingCart == null ? new Cart(user, restaurant) : existingCart;
        });
    }
    public Cart getCart(String userId) {
        return activeCarts.get(userId);
    }

    public void clearCart(String userId) {
        activeCarts.remove(userId);
    }
}

class RestaurantService{
    private static RestaurantService instance;
    private final Map<String, Restaurant> restaurants;
    private RestaurantService(){
        this.restaurants = new ConcurrentHashMap<>();
    };
    public static RestaurantService getInstance(){
        if(instance == null)
            instance = new RestaurantService();
        return instance;
    }

    public Restaurant addRestaurant(Address address, String name){
        Restaurant restaurant = new Restaurant(address, UUID.randomUUID().toString(), name );
        restaurants.put(restaurant.getId(), restaurant);
        return restaurant;
    }

    public boolean addItem(String name, double price, String restaurantId){
        Restaurant restaurant = restaurants.get(restaurantId);
        if(restaurant == null)
            return false;
        restaurant.addToMenu(new MenuItem(UUID.randomUUID().toString(), name, price));
        return true;
    }

    public List<Restaurant> getAllRestaurant(){
        return restaurants.values().stream().toList();
    }

    public Restaurant get(String restaurantId){
        return restaurants.get(restaurantId);
    }

}
class CustomerService {
    private final Map<String, Customer> cutsomers = new ConcurrentHashMap<>();
    private static CustomerService instance;
    private CustomerService(){};
    public static CustomerService getInstance(){
        if(instance == null)
            instance = new CustomerService();
        return instance;
    }

    public Customer addCustomer(String name, String email, String number, Address address){
        Customer c = new Customer(UUID.randomUUID().toString(), name, email,number, address);
        cutsomers.put(c.getUserId(), c);
        return c;
    }

    public Customer getCustomer(String customerID){
        return cutsomers.get(customerID);
    }

}

class OrderService {
    final Map<String, Order> orders = new ConcurrentHashMap<>();
    private static OrderService instance;
    private OrderService(){};
    public static OrderService getInstance(){
        if(instance == null)
            instance = new OrderService();
        return instance;
    }
    Order createOrder(Customer c, Cart cart) {
        List<OrderItem> items = cart.getItems().entrySet().stream()
                .map(e -> new OrderItem(e.getValue(), e.getKey()))
                .toList();

        Order o = new Order(UUID.randomUUID().toString(), cart.getRestaurant(), c, c.getDefaultAddress() );
        orders.put(o.getOrderId(), o);
        return o;
    }

    public Order getOrder(String orderId){
        return orders.get(orderId);
    }



    public boolean cancelOrder(String orderId) {
        Order o = orders.get(orderId);
        if (o != null && o.getStatus() == OrderStatus.PENDING) {
            o.setStatus(OrderStatus.CANCELLED);
            return true;
        }
        return false;
    }

    public void setStatus(String orderId, OrderStatus status){
        Order o = orders.get(orderId);
        if (o != null) o.setStatus(status);
    }
}
interface DeliveryAssignmentStrategy {
    DeliveryPartner assign(List<DeliveryPartner> partners, Order order);
}
class NearestDeliveryPartnerStrategy implements DeliveryAssignmentStrategy {
    public DeliveryPartner assign(List<DeliveryPartner> partners, Order order) {
        Address restaurantAddress = order.getRestaurant().getAddress();
        Address customerAddress = order.getAddress();
        return partners.stream()
                .filter(p -> p.getIsAvailable().get())
                .min(Comparator.comparingDouble(
                        p -> calculateTotalDistance(p, restaurantAddress, customerAddress)))
                .orElseThrow(() -> new RuntimeException("No delivery partner available"));
    }

    public double calculateTotalDistance(DeliveryPartner agent, Address restaurantAddress, Address customerAddress){
        double agentToRestaurantDist = agent.getCurrentLocation().distanceTo(restaurantAddress);
        double restaurantToCustomerDist = restaurantAddress.distanceTo(customerAddress);
        return agentToRestaurantDist + restaurantToCustomerDist;
    }
}
class DeliveryService {
    final List<DeliveryPartner> partners = new CopyOnWriteArrayList<>();
    private static DeliveryService instance;
    private DeliveryService(){};
    public static DeliveryService getInstance(){
        if(instance == null)
            instance = new DeliveryService();
        return instance;
    }
    public DeliveryPartner register(String  name, String email, String phoneNumber, Address currentLocation) {
        DeliveryPartner dp = new DeliveryPartner(UUID.randomUUID().toString(),name, email, phoneNumber, currentLocation );
        partners.add(dp);
        return dp;
    }

    DeliveryPartner assign(Order order, DeliveryAssignmentStrategy strategy) {
        DeliveryPartner p = strategy.assign(partners, order);
        p.setAvailable(false);
        return p;
    }

}

interface PaymentStrategy {
    boolean pay(double amount);
}

class UpiPayment implements PaymentStrategy {
    public boolean pay(double amount) {
        System.out.println("💰 Paid ₹" + amount + " via UPI");
        return true;
    }
}

interface RestaurantSearchStrategy {
    List<Restaurant> filter(List<Restaurant> allRestaurants);
}

class SearchByCityStrategy implements RestaurantSearchStrategy {
    private final String city;

    public SearchByCityStrategy(String city) {
        this.city = city;
    }

    @Override
    public List<Restaurant> filter(List<Restaurant> allRestaurants) {
        return allRestaurants.stream()
                .filter(r -> r.getAddress().getCity().equalsIgnoreCase(city))
                .collect(Collectors.toList());
    }
}

class FoodDeliveryService{
    private final CustomerService customerService;
    private final RestaurantService restaurantService;
    private final CartService cartService;
    private final OrderService orderService;
    private final DeliveryService deliveryService;
    private static FoodDeliveryService instance;
    private FoodDeliveryService(){
        this.cartService = CartService.getInstance();
        this.customerService = CustomerService.getInstance();
        this.restaurantService = RestaurantService.getInstance();
        this.orderService = OrderService.getInstance();
        this.deliveryService = DeliveryService.getInstance();
    }
    private static FoodDeliveryService getInstance(){
        if(instance == null)
            instance = new FoodDeliveryService();
        return instance;
    }
    public Customer registerCustomer(String name, String email, String phoneNumber, Address address) {
        return customerService.addCustomer(name, email, phoneNumber, address);
    }
    public Restaurant createRestaurant(String name, Address address) {
        return restaurantService.addRestaurant(address,  name);
    }
    public DeliveryPartner registerDeliveryPartner(String name, String email, String phoneNumber, Address address){
        return this.deliveryService.register(name, email, phoneNumber, address);
    }
    public Order placeOrder(String customerId, String restaurantId){
        Customer customer = customerService.getCustomer(customerId);
        Restaurant restaurant = restaurantService.get(restaurantId);
        Cart cart = cartService.getCart(customerId);
        if (customer == null || restaurant == null || cart == null) throw new NoSuchElementException("Customer or Restaurant or Cart not found.");
        Order order = this.orderService.createOrder(customer, cart);
        customer.addOrderToHistory(order);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
    public void cancelOrder(String orderId) {
        // Delegate the cancellation logic to the Order object itself.
        if (orderService.cancelOrder(orderId)) {
            System.out.println("SUCCESS: Order " + orderId + " has been successfully canceled.");
        } else {
            System.out.println("FAILED: Order " + orderId + " could not be canceled");
        }
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderService.getOrder(orderId);
        if (order == null)
            throw new NoSuchElementException("Order not found.");

        order.setStatus(newStatus);

        // If order is ready, find a delivery agent.
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            assignDelivery(order);
        }
    }

    private void assignDelivery(Order order) {
        System.out.println("Searching for Delivery Partner for Order: " + order.getOrderId());
        try {
            // Using Nearest Strategy by default
            DeliveryPartner partner = deliveryService.assign(order, new NearestDeliveryPartnerStrategy());

            // Link partner to order and update status
            // Note: Added setDeliveryPartner to Order class below
            order.setDeliveryPartner(partner);
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

            System.out.println("Partner " + partner.getName() + " assigned to Order " + order.getOrderId());
        } catch (Exception e) {
            System.err.println("Delivery Assignment Failed: " + e.getMessage());
            // In production, we would use a retry queue here
        }
    }
    public List<Restaurant> searchRestaurants(List<RestaurantSearchStrategy> strategies) {
        List<Restaurant> results = restaurantService.getAllRestaurant();
        for (RestaurantSearchStrategy strategy : strategies) {
            results = strategy.filter(results);
        }
        return results;
    }

    public Menu getRestaurantMenu(String restaurantId) {
        Restaurant restaurant = restaurantService.get(restaurantId);
        if (restaurant == null) throw new NoSuchElementException("Restaurant not found");
        return restaurant.getMenu();
    }

    public void addItemToCart(String customerId, String restaurantId, String itemId, int qty) {
        Customer customer = customerService.getCustomer(customerId);
        Restaurant restaurant = restaurantService.get(restaurantId);
        MenuItem item = restaurant.getMenu().getItem(itemId);

        Cart cart = cartService.getOrCreateCart(customer, restaurant);
        cart.addItem(item, qty);
    }

}

public class FoodDeliverySystem {
}
