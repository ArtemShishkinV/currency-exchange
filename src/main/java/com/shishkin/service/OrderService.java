package com.shishkin.service;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Order createOrder(OrderOperationDto orderOperationDto) throws NotEnoughMoneyException;

    void execute(Order order, Order anotherOrder);

    void revoke(Order order);

    List<Order> getActiveOrders(Map<CurrencyPair, List<Order>> orders);

    List<Order> getAllOrders(Map<CurrencyPair, List<Order>> orders);
}
