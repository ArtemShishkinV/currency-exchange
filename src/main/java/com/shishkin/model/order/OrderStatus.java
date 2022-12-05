package com.shishkin.model.order;

public enum OrderStatus {
    NEW, PARTIALLYFILL, FILL, CANCELLED;

    public static boolean isActiveOrder(Order order) {
        return NEW.equals(order.getStatus()) || PARTIALLYFILL.equals(order.getStatus());
    }
}
