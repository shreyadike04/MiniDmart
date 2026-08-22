package com.minidmart.model;

import java.time.LocalDateTime;

public class ReturnRequest {
    public enum Type { RETURN, EXCHANGE }
    public enum Status { REQUESTED, APPROVED, REJECTED, COMPLETED }

    private int returnId;
    private int orderItemId;
    private int orderId;
    private String orderNumber;
    private String productName;
    private int userId;
    private String customerName;
    private Type type;
    private String reason;
    private int quantity;
    private Status status;
    private Integer exchangeProductId;
    private String exchangeProductName;
    private String staffNotes;
    private Integer resolvedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;

    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getExchangeProductId() { return exchangeProductId; }
    public void setExchangeProductId(Integer exchangeProductId) { this.exchangeProductId = exchangeProductId; }

    public String getExchangeProductName() { return exchangeProductName; }
    public void setExchangeProductName(String exchangeProductName) { this.exchangeProductName = exchangeProductName; }

    public String getStaffNotes() { return staffNotes; }
    public void setStaffNotes(String staffNotes) { this.staffNotes = staffNotes; }

    public Integer getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Integer resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
