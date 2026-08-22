package com.minidmart.tools;

import com.minidmart.dao.CategoryDao;
import com.minidmart.dao.PickupSlotDao;
import com.minidmart.dao.ProductDao;
import com.minidmart.dao.UserDao;
import com.minidmart.model.*;
import com.minidmart.util.PasswordUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static java.util.Map.entry;

/**
 * One-shot demo data loader: run after schema.sql to populate categories,
 * products, pickup slots and one test account per role. Not part of the web
 * app's request flow — invoked manually from the command line.
 *
 * Usage: java -cp <classes>;<mysql-connector>;<jstl> com.minidmart.tools.DataSeeder
 */
public final class DataSeeder {

    /** Real product photos (Wikimedia Commons, stable Special:FilePath links) keyed by SKU. */
    private static final Map<String, String> IMAGES = Map.ofEntries(
            entry("FRV-001", "https://commons.wikimedia.org/wiki/Special:FilePath/Bananas.jpg?width=500"),
            entry("FRV-002", "https://commons.wikimedia.org/wiki/Special:FilePath/Tomato_je.jpg?width=500"),
            entry("FRV-003", "https://commons.wikimedia.org/wiki/Special:FilePath/Onion_on_White.JPG?width=500"),
            entry("FRV-004", "https://commons.wikimedia.org/wiki/Special:FilePath/Patates.jpg?width=500"),
            entry("FRV-005", "https://commons.wikimedia.org/wiki/Special:FilePath/Spinach.jpg?width=500"),
            entry("FRV-006", "https://commons.wikimedia.org/wiki/Special:FilePath/Red_Apple.jpg?width=500"),
            entry("DRY-001", "https://commons.wikimedia.org/wiki/Special:FilePath/Milk.jpg?width=500"),
            entry("DRY-002", "https://commons.wikimedia.org/wiki/Special:FilePath/Yoghurt%20in%20bowl%20011715.jpg?width=500"),
            entry("DRY-003", "https://commons.wikimedia.org/wiki/Special:FilePath/Homemade%20Paneer%20cottage%20cheese%20cut%20into%20cubes.JPG?width=500"),
            entry("DRY-004", "https://commons.wikimedia.org/wiki/Special:FilePath/Egg%20cartons%20with%20chicken%20eggs%2003.jpg?width=500"),
            entry("DRY-005", "https://commons.wikimedia.org/wiki/Special:FilePath/Block%20of%20butter%20in%20butter%20dish.jpg?width=500"),
            entry("BAK-001", "https://commons.wikimedia.org/wiki/Special:FilePath/Transparent%20Slice%20of%20Sara%20Lee%20white%20whole%20grain%20bread%20in%20the%20Franklin%20Farm%20section%20of%20Oak%20Hill%2C%20Fairfax%20County%2C%20Virginia.png?width=500"),
            entry("BAK-002", "https://commons.wikimedia.org/wiki/Special:FilePath/Vegan%20no-knead%20whole%20wheat%20bread%20loaf%2C%20sliced%2C%20September%202010.jpg?width=500"),
            entry("BAK-003", "https://commons.wikimedia.org/wiki/Special:FilePath/Bulkie%20roll%20spicy%20salmon%20burger.jpg?width=500"),
            entry("BEV-001", "https://commons.wikimedia.org/wiki/Special:FilePath/Orange%20juice%201%20edit1.jpg?width=500"),
            entry("BEV-002", "https://commons.wikimedia.org/wiki/Special:FilePath/Drink%20%22Favorite%20Cola%22.jpg?width=500"),
            entry("BEV-003", "https://commons.wikimedia.org/wiki/Special:FilePath/Green%20Tea%20Cup%20with%20Hollandia%20milk.jpg?width=500"),
            entry("BEV-004", "https://commons.wikimedia.org/wiki/Special:FilePath/Instant%20Coffee%20In%20a%20Glass%20Jar.jpg?width=500"),
            entry("SNK-001", "https://commons.wikimedia.org/wiki/Special:FilePath/Chips%20in%20a%20bowl%20at%20a%20party.JPG?width=500"),
            entry("SNK-002", "https://commons.wikimedia.org/wiki/Special:FilePath/Digestive%20biscuits.jpg?width=500"),
            entry("SNK-003", "https://commons.wikimedia.org/wiki/Special:FilePath/Bombaymix.jpg?width=500"),
            entry("HHD-001", "https://commons.wikimedia.org/wiki/Special:FilePath/Tesco%20and%20Sainsburys%20own%20dishwashing%20liquid.jpg?width=500"),
            entry("HHD-002", "https://commons.wikimedia.org/wiki/Special:FilePath/Laundry%20detergent%201.jpg?width=500"),
            entry("HHD-003", "https://commons.wikimedia.org/wiki/Special:FilePath/Stacked%20rolls%20of%20toilet%20paper%2C%20Tunnicliff's%20Tavern.jpg?width=500")
    );

    public static void main(String[] args) throws Exception {
        UserDao userDao = new UserDao();
        CategoryDao categoryDao = new CategoryDao();
        ProductDao productDao = new ProductDao();
        PickupSlotDao slotDao = new PickupSlotDao();

        System.out.println("Seeding users...");
        seedUser(userDao, "Admin User", "admin@minidmart.com", "Admin@123", Role.ADMIN);
        seedUser(userDao, "Staff User", "staff@minidmart.com", "Staff@123", Role.STAFF);
        seedUser(userDao, "Demo Customer", "customer@minidmart.com", "Customer@123", Role.CUSTOMER);

        System.out.println("Seeding categories...");
        int fruits = seedCategory(categoryDao, "Fruits & Vegetables", "Fresh seasonal produce");
        int dairy = seedCategory(categoryDao, "Dairy & Eggs", "Milk, curd, cheese, eggs");
        int bakery = seedCategory(categoryDao, "Bakery", "Bread, buns and baked goods");
        int beverages = seedCategory(categoryDao, "Beverages", "Juices, soft drinks, tea & coffee");
        int snacks = seedCategory(categoryDao, "Snacks", "Chips, biscuits and namkeen");
        int household = seedCategory(categoryDao, "Household", "Cleaning and daily essentials");

        System.out.println("Seeding products...");
        seedProduct(productDao, fruits, "FRV-001", "Banana", "Fresh ripe bananas", "1 dozen", "60", 30, 8);
        seedProduct(productDao, fruits, "FRV-002", "Tomato", "Farm fresh tomatoes", "1 kg", "40", 100, 20);
        seedProduct(productDao, fruits, "FRV-003", "Onion", "Regular onions", "1 kg", "35", 100, 20);
        seedProduct(productDao, fruits, "FRV-004", "Potato", "Regular potatoes", "1 kg", "30", 120, 20);
        seedProduct(productDao, fruits, "FRV-005", "Spinach", "Bunch of fresh spinach", "250 g", "20", 40, 10);
        seedProduct(productDao, fruits, "FRV-006", "Apple (Shimla)", "Crisp red apples", "1 kg", "180", 25, 8);

        seedProduct(productDao, dairy, "DRY-001", "Toned Milk", "Fresh toned milk pouch", "500 ml", "28", 80, 20);
        seedProduct(productDao, dairy, "DRY-002", "Curd", "Fresh dahi", "400 g", "35", 50, 15);
        seedProduct(productDao, dairy, "DRY-003", "Paneer", "Fresh cottage cheese", "200 g", "80", 30, 10);
        seedProduct(productDao, dairy, "DRY-004", "Farm Eggs", "White eggs", "6 pcs", "48", 40, 10);
        seedProduct(productDao, dairy, "DRY-005", "Butter", "Salted table butter", "100 g", "55", 35, 10);

        seedProduct(productDao, bakery, "BAK-001", "White Bread", "Soft sandwich bread", "400 g", "42", 30, 8);
        seedProduct(productDao, bakery, "BAK-002", "Multigrain Bread", "Whole wheat multigrain loaf", "400 g", "55", 20, 6);
        seedProduct(productDao, bakery, "BAK-003", "Burger Buns", "Soft burger buns", "4 pcs", "45", 25, 6);

        seedProduct(productDao, beverages, "BEV-001", "Orange Juice", "100% orange juice", "1 L", "120", 24, 8);
        seedProduct(productDao, beverages, "BEV-002", "Cola", "Carbonated soft drink", "750 ml", "40", 48, 12);
        seedProduct(productDao, beverages, "BEV-003", "Green Tea", "25 tea bags", "pack", "150", 20, 5);
        seedProduct(productDao, beverages, "BEV-004", "Instant Coffee", "Classic instant coffee", "100 g", "220", 18, 5);

        seedProduct(productDao, snacks, "SNK-001", "Potato Chips", "Classic salted chips", "70 g", "20", 60, 15);
        seedProduct(productDao, snacks, "SNK-002", "Digestive Biscuits", "Whole wheat biscuits", "250 g", "35", 45, 12);
        seedProduct(productDao, snacks, "SNK-003", "Mixed Namkeen", "Traditional savory mix", "200 g", "60", 30, 8);

        seedProduct(productDao, household, "HHD-001", "Dish Wash Liquid", "Lemon dish wash", "500 ml", "99", 3, 15);
        seedProduct(productDao, household, "HHD-002", "Laundry Detergent", "Top load detergent powder", "1 kg", "160", 22, 8);
        seedProduct(productDao, household, "HHD-003", "Toilet Paper", "Pack of 4 rolls", "pack", "110", 0, 10);

        System.out.println("Seeding pickup slots (next 5 days)...");
        LocalTime[][] windows = {
                {LocalTime.of(10, 0), LocalTime.of(12, 0)},
                {LocalTime.of(12, 0), LocalTime.of(14, 0)},
                {LocalTime.of(16, 0), LocalTime.of(18, 0)}
        };
        for (int d = 1; d <= 5; d++) {
            LocalDate date = LocalDate.now().plusDays(d);
            for (LocalTime[] w : windows) {
                PickupSlot slot = new PickupSlot();
                slot.setSlotDate(date);
                slot.setStartTime(w[0]);
                slot.setEndTime(w[1]);
                slot.setCapacity(8);
                slotDao.create(slot);
            }
        }

        System.out.println("Done. Test accounts:");
        System.out.println("  admin@minidmart.com / Admin@123");
        System.out.println("  staff@minidmart.com / Staff@123");
        System.out.println("  customer@minidmart.com / Customer@123");
    }

    private static void seedUser(UserDao dao, String name, String email, String password, Role role) throws Exception {
        if (dao.findByEmail(email).isPresent()) {
            System.out.println("  user exists, skipping: " + email);
            return;
        }
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setPhone("9999999999");
        u.setRole(role);
        dao.create(u);
    }

    private static int seedCategory(CategoryDao dao, String name, String description) throws Exception {
        for (Category c : dao.listAll()) {
            if (c.getName().equalsIgnoreCase(name)) return c.getCategoryId();
        }
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return dao.create(c);
    }

    private static void seedProduct(ProductDao dao, int categoryId, String sku, String name, String description,
                                     String unit, String price, int stockQty, int reorderLevel) throws Exception {
        for (Product p : dao.listAll()) {
            if (p.getSku().equalsIgnoreCase(sku)) return;
        }
        Product p = new Product();
        p.setCategoryId(categoryId);
        p.setSku(sku);
        p.setName(name);
        p.setDescription(description);
        p.setUnit(unit);
        p.setPrice(new BigDecimal(price));
        p.setImageUrl(IMAGES.get(sku));
        p.setStockQty(stockQty);
        p.setReorderLevel(reorderLevel);
        p.setActive(true);
        dao.create(p);
    }
}
