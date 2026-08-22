package com.minidmart.model;

public enum OrderStatus {
    PLACED, CONFIRMED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, COMPLETED, CANCELLED;

    public boolean isCancellable() {
        return this == PLACED || this == CONFIRMED || this == PREPARING;
    }

    public boolean isReturnEligible() {
        return this == COMPLETED;
    }
}
