package com.shishkin.service.implementation.simple;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.service.AbstractExchangeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeServiceImpl extends AbstractExchangeService {
    private static final Map<CurrencyPair, List<Order>> orders = new ConcurrentHashMap<>();

    public ExchangeServiceImpl(Set<CurrencyPair> currencyPairs) {
        for (CurrencyPair currencyPair : currencyPairs) {
            orders.put(currencyPair, new ArrayList<>());
        }
    }

    @Override
    public void createOrder(OrderOperationDto orderOperationDto) {
        synchronized (orderOperationDto.getCurrencyPair()) {
            try {
                Order order = ORDER_SERVICE.createOrder(orderOperationDto);
                ORDER_SERVICE.processOrder(order, orders.get(orderOperationDto.getCurrencyPair()));
            } catch (NotEnoughMoneyException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public List<Order> getOrders() {
        return ORDER_SERVICE.getActiveOrders(Collections.unmodifiableMap(orders));
    }

}
