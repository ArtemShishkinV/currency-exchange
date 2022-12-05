package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.model.order.OrderStatus;
import com.shishkin.service.ClientService;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.OrderService;
import com.shishkin.utils.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeServiceImpl implements ExchangeService {
    private static final OrderService ORDER_SERVICE = new OrderServiceImpl();
    private static final ClientService CLIENT_SERVICE = new ClientServiceImpl();
    private static final Map<CurrencyPair, List<Order>> orders = new ConcurrentHashMap<>();

    public ExchangeServiceImpl(Set<CurrencyPair> currencyPairs) {
        for (CurrencyPair currencyPair : currencyPairs) {
            orders.put(currencyPair, new ArrayList<>());
        }
    }

    @Override
    public Client createClient() {
        return CLIENT_SERVICE.create();
    }

    @Override
    public void deposit(ClientOperationDto clientOperationDto) {
        try {
            CLIENT_SERVICE.deposit(clientOperationDto);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void withdraw(ClientOperationDto clientOperationDto) {
        try {
            CLIENT_SERVICE.withdraw(clientOperationDto);
        } catch (NotEnoughMoneyException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void createOrder(OrderOperationDto orderOperationDto) {
        synchronized (orderOperationDto.getCurrencyPair()) {
            try {
                Order order = ORDER_SERVICE.createOrder(orderOperationDto);
                List<Order> matchOrders = orders.get(order.getCurrencyPair()).stream()
                        .filter(item -> matchOrdersFilter(item, order))
                        .sorted(Comparator.comparing(Order::getPrice))
                        .toList();
                for (Order matchOrder :
                        matchOrders) {
                    ORDER_SERVICE.execute(matchOrder, order);
                    if (OrderStatus.FILL.equals(matchOrder.getStatus())) ORDER_SERVICE.revoke(matchOrder);
                    if (OrderStatus.FILL.equals(order.getStatus())) {
                        ORDER_SERVICE.revoke(order);
                        return;
                    }
                }
                if (order.getAmount().compareTo(BigDecimalUtils.round(BigDecimal.ZERO)) > 0) {
                    orders.get(orderOperationDto.getCurrencyPair()).add(order);
                }
            } catch (NotEnoughMoneyException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public List<Order> getOrders() {
        return ORDER_SERVICE.getActiveOrders(orders);
    }

    @Override
    public String getInfo(Client client) {
        return CLIENT_SERVICE.getInfo(client);
    }

    private boolean matchOrdersFilter(Order order, Order anotherOrder) {
        return filterByType(order, anotherOrder) && filterByPrice(order, anotherOrder) && filterByClient(order, anotherOrder)
                && OrderStatus.isActiveOrder(order) && OrderStatus.isActiveOrder(anotherOrder);
    }

    private boolean filterByType(Order order, Order anotherOrder) {
        return !order.getOrderDirection().equals(anotherOrder.getOrderDirection())
                && !order.getCurrencyPair().getTo().equals(anotherOrder.getCurrencyPair().getTo());
    }

    private boolean filterByClient(Order order, Order anotherOrder) {
        return !order.getClient().equals(anotherOrder.getClient());
    }

    private boolean filterByPrice(Order order, Order anotherOrder) {
        if (OrderDirection.BUY.equals(order.getOrderDirection())) {
            return order.getPrice().compareTo(anotherOrder.getPrice()) >= 0;
        } else {
            return order.getPrice().compareTo(anotherOrder.getPrice()) <= 0;
        }
    }


}
