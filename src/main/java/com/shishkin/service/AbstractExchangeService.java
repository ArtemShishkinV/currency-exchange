package com.shishkin.service;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.service.implementation.ClientServiceImpl;
import com.shishkin.service.implementation.OrderServiceImpl;

public abstract class AbstractExchangeService implements ExchangeService {
    protected static final OrderService ORDER_SERVICE = new OrderServiceImpl();
    protected static final ClientService CLIENT_SERVICE = new ClientServiceImpl();

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
    public String getInfo(Client client) {
        return CLIENT_SERVICE.getInfo(client);
    }
}
