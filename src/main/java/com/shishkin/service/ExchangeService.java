package com.shishkin.service;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.order.Order;

import java.util.List;

public interface ExchangeService {
    Client createClient();

    void deposit(ClientOperationDto clientOperationDto);

    void withdraw(ClientOperationDto clientOperationDto);

    void createOrder(OrderOperationDto orderOperationDto);

    List<Order> getOrders();

    String getInfo(Client client);
}
