package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderDirection;
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
    private final Map<CurrencyPair, List<Order>> orders;
    private final OrderService orderService = new OrderServiceImpl();
    private final ClientService clientService = new ClientServiceImpl();

    public ExchangeServiceImpl(Set<CurrencyPair> currencyPairs) {
        this.orders = new ConcurrentHashMap<>(currencyPairs.size());
        for (CurrencyPair currencyPair : currencyPairs) {
            orders.put(currencyPair, new ArrayList<>());
        }
    }

    @Override
    public Client createClient() {
        return clientService.create();
    }

    @Override
    public void deposit(ClientOperationDto clientOperationDto) {
        try {
            clientService.deposit(clientOperationDto);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void withdraw(ClientOperationDto clientOperationDto) {
        try {
            clientService.withdraw(clientOperationDto);
        } catch (NotEnoughMoneyException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void createOrder(OrderOperationDto orderOperationDto) {
        try {
            Order order = orderService.createOrder(orderOperationDto);
            synchronized (order.getCurrencyPair()) {
                List<Order> matchOrders = orders.get(order.getCurrencyPair()).stream()
                        .filter(item -> matchOrdersFilter(item, order))
                        .sorted(Comparator.comparing(Order::getPrice))
                        .toList();
                matchOrders.forEach(matchOrder -> {
                    BigDecimal amount = matchOrder.getAmount().min(order.getAmount());
                    BigDecimal price = getPriceByOrderDirection(matchOrder, order);
                    System.out.println("#match: "+ order.getId() + " - " + matchOrder.getId());

                    orderService.execute(matchOrder, order);
                });
                if (order.getAmount().compareTo(BigDecimalUtils.round(BigDecimal.ZERO)) > 0) {
                    orders.get(orderOperationDto.getCurrencyPair()).add(order);
                }
            }

        } catch (NotEnoughMoneyException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Order> getOrders() {
        return null;
    }

    @Override
    public void getInfo(Client client) {
        System.out.println(clientService.getInfo(client));
    }

    private boolean matchOrdersFilter(Order order, Order anotherOrder) {
        return filterByType(order, anotherOrder) && filterByPrice(order, anotherOrder);
    }

    private boolean filterByType(Order order, Order anotherOrder) {
        return !order.getOrderDirection().equals(anotherOrder.getOrderDirection());
    }

    private boolean filterByPrice(Order order, Order anotherOrder) {
        if(OrderDirection.BUY.equals(order.getOrderDirection())) {
            return order.getPrice().compareTo(anotherOrder.getPrice()) >= 0;
        } else {
            return order.getPrice().compareTo(anotherOrder.getPrice()) <= 0;
        }
    }

    private BigDecimal getPriceByOrderDirection(Order order, Order anotherOrder) {
        if(OrderDirection.BUY.equals(order.getOrderDirection())) {
            return order.getPrice().min(anotherOrder.getPrice());
        }
        return order.getPrice().max(anotherOrder.getPrice());
    }
}
