package com.minidmart.model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private int categoryId;
    private String categoryName;
    private String sku;
    private String name;
    private String description;
    private String unit;
    private BigDecimal price;
    private String imageUrl;
    private int stockQty;
    private int reorderLevel;
    private boolean active;

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isLowStock() { return stockQty <= reorderLevel; }
    public boolean isInStock() { return stockQty > 0; }

    /**
     * Filename (without extension) of the local category icon under
     * /assets/images/categories/ to show when the product has no imageUrl of
     * its own. Falls back to a generic grocery icon for any category name
     * added later (via admin) that isn't one of the seeded ones.
     */
    public String getCategoryIconSlug() {
        if (categoryName == null) return "grocery";
        switch (categoryName) {
            case "Fruits & Vegetables": return "fruits-veg";
            case "Dairy & Eggs": return "dairy";
            case "Bakery": return "bakery";
            case "Beverages": return "beverages";
            case "Snacks": return "snacks";
            case "Household": return "household";
            default: return "grocery";
        }
    }
}
