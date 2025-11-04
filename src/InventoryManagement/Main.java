package InventoryManagement;

import java.util.*;

enum ProductCategory {
    ELECTRONICS, CLOTHING, GROCERY, FURNITURE, OTHER
}
abstract class Product {
    private String sku;
    private String name;
    private int quantity;
    private int threshold;
    private double price;
    private ProductCategory category;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }
}

class ElectronicsProduct extends Product {
    private String brand;
    private int warrantyPeriod; // in months

    public ElectronicsProduct(String sku, String name, double price, int quantity, int threshold) {
        super();
        setSku(sku);
        setName(name);
        setPrice(price);
        setQuantity(quantity);
        setCategory(ProductCategory.ELECTRONICS);
        setThreshold(threshold);
    }

    // Getters and setters for electronics-specific attributes
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
class ClothingProduct extends Product {
    private String size;
    private String color;

    public ClothingProduct(String sku, String name, double price, int quantity,int threshold) {
        super();
        setSku(sku);
        setName(name);
        setPrice(price);
        setQuantity(quantity);
        setCategory(ProductCategory.CLOTHING);
        setThreshold(threshold);
    }

    // Getters and setters for clothing-specific attributes
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

class GroceryProduct extends Product {
    private Date expiryDate;
    private boolean refrigerated;

    public GroceryProduct(String sku, String name, double price, int quantity, int threshold) {
        super();
        setSku(sku);
        setName(name);
        setPrice(price);
        setQuantity(quantity);
        setCategory(ProductCategory.GROCERY);
        setThreshold(threshold);
    }

    // Getters and setters for grocery-specific attributes
    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRefrigerated() {
        return refrigerated;
    }

    public void setRefrigerated(boolean refrigerated) {
        this.refrigerated = refrigerated;
    }
}

class ProductFactory {
    public Product createProduct(ProductCategory category, String sku, String name, double price, int quantity, int threshold) {
        return switch (category) {
            case ELECTRONICS -> new ElectronicsProduct(sku, name, price, quantity, threshold);
            case CLOTHING -> new ClothingProduct(sku, name, price, quantity, threshold);
            case GROCERY -> new GroceryProduct(sku, name, price, quantity, threshold);
            default -> throw new IllegalArgumentException(
                    "Unsupported product category: " + category);
        };
    }
}
interface ReplenishmentStrategy {
    // Method to replenish stock for a given product
    void replenish(Product product);
}
class BulkOrderStrategy implements ReplenishmentStrategy {
    @Override
    public void replenish(Product product) {

        System.out.println("Applying Bulk Order replenishment for " + product.getName());
        int amountToOrder = Math.max(product.getThreshold()* 2, 100);
        product.setQuantity(product.getQuantity()+ amountToOrder);
        // Order in large quantities to minimize order costs
    }
}

class Warehouse{
    private int id;
    private Map<String, Product> products;
    private String name;
    private String location;
    public Warehouse(int id, String name, String location){
        this.id = id;
        this.name = name;
        this.location = location;
        products = new HashMap<>();
    }
    public void addProduct(Product product, int quantity){
        String sku = product.getSku();
        if (products.containsKey(sku)) {
            // Product exists, update quantity
            Product existingProduct = products.get(sku);
            existingProduct.setQuantity(existingProduct.getQuantity() + quantity);
        } else {
            // New product, add to inventory
            product.setQuantity(quantity);
            products.put(sku, product);
        }
        System.out.println(quantity + " units of " + product.getName()
                + " (SKU: " + sku + ") added to " + name
                + ". New quantity: " + getAvailableQuantity(sku));
    }
    public boolean removeProduct(String sku, int quantity){
        if (products.containsKey(sku)) {
            Product product = products.get(sku);
            int currentQuantity = product.getQuantity();
            if (currentQuantity >= quantity) {
                // Sufficient inventory to remove
                product.setQuantity(currentQuantity - quantity);
                System.out.println(quantity + " units of " + product.getName()
                        + " (SKU: " + sku + ") removed from " + name
                        + ". Remaining quantity: " + product.getQuantity());
                // If quantity becomes zero, consider removing the product entirely
                if (product.getQuantity() == 0) {
                    // Remove products with zero quantity
                    products.remove(sku);
                    System.out.println("Product " + product.getName()
                            + " removed from inventory as quantity is now zero.");
                }
                return true;
            } else {
                System.out.println("Error: Insufficient inventory. Requested: "
                        + quantity + ", Available: " + currentQuantity);
                return false;
            }
        } else {
            System.out.println(
                    "Error: Product with SKU " + sku + " not found in " + name);
            return false;
        }
    }
    public int getAvailableQuantity(String sku) {
        if (products.containsKey(sku)) {
            return products.get(sku).getQuantity();
        }
        return 0; // Product not found
    }
    public Product getProductBySku(String sku){
        if (products.containsKey(sku)) {
            return products.get(sku);
        }
        return null;
    }
    public Collection<Product> getAllProducts(){
        return products.values();
    }

}
class InventoryManager {
    private static List<Warehouse> warehouses;
    private ProductFactory productFactory;
    private ReplenishmentStrategy replenishmentStrategy;
    private InventoryManager() {
        // Initialize collections and dependencies
        warehouses = new ArrayList<>();
        productFactory = new ProductFactory();
    }
    private static final class InstanceHolder {
        private static final InventoryManager instance = new InventoryManager();
    }

    public static InventoryManager getInstance(){
        return InstanceHolder.instance;
    }
    public void setReplenishmentStrategy(ReplenishmentStrategy strategy) {
        this.replenishmentStrategy = strategy;
    }
    public void addWarehouse(Warehouse warehouse) {
        warehouses.add(warehouse);
    }
    public void removeWarehouse(Warehouse warehouse) {
        warehouses.remove(warehouse);
    }
    public Product getProductBySku(String sku){
        for (Warehouse warehouse : warehouses) {
            Product product = warehouse.getProductBySku(sku);
            if (product != null) {
                return product;
            }
        }
        return null;
    }
    public void checkAndReplenish(String sku) {
        Product product = getProductBySku(sku);
        if (product != null) {
            // If product is below threshold, notify observers
            if (product.getQuantity() < product.getThreshold()) {

                // Apply current replenishment strategy
                if (replenishmentStrategy != null) {
                    replenishmentStrategy.replenish(product);
                }
            }
        }
    }
    public void performInventoryCheck() {
        for (Warehouse warehouse : warehouses) {
            for (Product product : warehouse.getAllProducts()) {
                if (product.getQuantity() < product.getThreshold()) {

                    if (replenishmentStrategy != null) {
                        replenishmentStrategy.replenish(product);
                    }
                }
            }
        }
    }
}
public class Main {
    public static void main(String[] args) {
        InventoryManager inventoryManager = InventoryManager.getInstance();
        Warehouse warehouse1 = new Warehouse(1, "Warehouse 1", "Hyderabad" );
        Warehouse warehouse2 = new Warehouse(1, "Warehouse 2", "Secunderabad");
        inventoryManager.addWarehouse(warehouse1);
        inventoryManager.addWarehouse(warehouse2);

        // Create products using ProductFactory
        ProductFactory productFactory = new ProductFactory();
        Product laptop = productFactory.createProduct(
                ProductCategory.ELECTRONICS, "SKU123", "Laptop", 1000.0, 50, 25);
        Product tShirt = productFactory.createProduct(
                ProductCategory.CLOTHING, "SKU456", "T-Shirt", 20.0, 200, 100);
        Product apple = productFactory.createProduct(
                ProductCategory.GROCERY, "SKU789", "Apple", 1.0, 100, 200);

        // Add products to warehouses
        warehouse1.addProduct(laptop, 15);
        warehouse1.addProduct(tShirt, 20);
        warehouse2.addProduct(apple, 50);

        // Set replenishment strategy to Just-In-Time
        inventoryManager.setReplenishmentStrategy(new BulkOrderStrategy());

        // Perform inventory check and replenish if needed
        inventoryManager.performInventoryCheck();

        // Switch replenishment strategy to Bulk Order
        inventoryManager.setReplenishmentStrategy(new BulkOrderStrategy());

        // Replenish a specific product if needed
        inventoryManager.checkAndReplenish("SKU123");
    }
}
