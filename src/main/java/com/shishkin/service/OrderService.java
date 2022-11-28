package com.shishkin.service;

import com.shishkin.model.currency.Currency;
import com.shishkin.model.order.Order;

import java.util.List;

public interface OrderService {
    void createOrder();

    List<Order> getOrders(Currency currency);

    List<Order> getAllOrders();
}
