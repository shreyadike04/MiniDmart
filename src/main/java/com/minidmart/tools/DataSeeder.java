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

/**
 * One-shot demo data loader: run after schema.sql to populate categories,
 * products, pickup slots and one test account per role. Not part of the web
 * app's request flow — invoked manually from the command line.
 *
 * Usage: java -cp <classes>;<mysql-connector>;<jstl> com.minidmart.tools.DataSeeder
 */
public final class DataSeeder {

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
        p.setStockQty(stockQty);
        p.setReorderLevel(reorderLevel);
        p.setActive(true);
        dao.create(p);
    }
}
