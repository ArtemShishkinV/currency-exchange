package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.service.ClientService;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeServiceImpl implements ExchangeService {
    private final Map<Currency, List<Order>> orders = new ConcurrentHashMap<>();
    private final OrderService orderService = new OrderServiceImpl();
    private final ClientService clientService = new ClientServiceImpl();

    public ExchangeServiceImpl(Set<CurrencyPair> currencyPairs) {

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
    public void createOrder() {

    }

    @Override
    public Order getOrders() {
        return null;
    }

    @Override
    public void getInfo(Client client) {
        System.out.println(clientService.getInfo(client));
    }
}
