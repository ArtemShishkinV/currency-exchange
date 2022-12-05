package com.shishkin.service;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.order.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderOperationDto orderOperationDto) throws NotEnoughMoneyException;

    void execute(Order order, Order anotherOrder);

    void revoke(Order order);

    List<Order> getOrders(Currency currency);

    List<Order> getAllOrders();
}
