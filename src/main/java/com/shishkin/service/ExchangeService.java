package com.shishkin.service;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.order.Order;

public interface ExchangeService {
    Client createClient();

    void deposit(ClientOperationDto clientOperationDto);

    void withdraw(ClientOperationDto clientOperationDto);

    Order createOrder(OrderOperationDto orderOperationDto);

    Order getOrders();

    void getInfo(Client client);
}
