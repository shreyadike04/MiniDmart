package com.minidmart.model;

import java.math.BigDecimal;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private String productNameSnap;
    private BigDecimal unitPriceSnap;
    private int quantity;
    private BigDecimal lineTotal;
    private boolean returnEligible;

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductNameSnap() { return productNameSnap; }
    public void setProductNameSnap(String productNameSnap) { this.productNameSnap = productNameSnap; }

    public BigDecimal getUnitPriceSnap() { return unitPriceSnap; }
    public void setUnitPriceSnap(BigDecimal unitPriceSnap) { this.unitPriceSnap = unitPriceSnap; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public boolean isReturnEligible() { return returnEligible; }
    public void setReturnEligible(boolean returnEligible) { this.returnEligible = returnEligible; }
}
