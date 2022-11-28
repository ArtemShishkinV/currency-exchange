package com.shishkin.service;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.order.Order;

public interface ExchangeService {
    Client createClient();

    void deposit(ClientOperationDto clientOperationDto);

    void withdraw(ClientOperationDto clientOperationDto);

    void createOrder();

    Order getOrders();

    void getInfo(Client client);
}
