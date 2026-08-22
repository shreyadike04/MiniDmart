package com.minidmart.model;

import java.math.BigDecimal;

public class CartItem {
    private int cartItemId;
    private int productId;
    private String productName;
    private String unit;
    private BigDecimal price;
    private int stockQty;
    private boolean productActive;
    private int quantity;

    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public boolean isProductActive() { return productActive; }
    public void setProductActive(boolean productActive) { this.productActive = productActive; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
}
