package com.shishkin.service.order;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.service.ClientService;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.ClientServiceImpl;
import com.shishkin.service.implementation.OrderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class OrderServiceTest {
    private static OrderService orderService;
    private static ClientService clientService;
    private static Client client;
    private static Client anotherClient;

    @BeforeAll
    static void setup() {
        clientService = new ClientServiceImpl();
        orderService = new OrderServiceImpl();
    }

    @BeforeEach
    void setupEach() {
        client = clientService.create();
        clientService.deposit(ClientOperationDto.create(client, Currency.RUB, 2000));
        anotherClient = clientService.create();
        clientService.deposit(ClientOperationDto.create(anotherClient, Currency.USD, 40));
    }

    @Test
    void createOrder() {
        List<Order> orders = new ArrayList<>();
        System.out.println(client + "\n" + anotherClient);
        for (int i = 0; i < 5; i ++) {
            Assertions.assertDoesNotThrow(() -> {
                Order order = orderService.createOrder(new OrderOperationDto(client,
                        new CurrencyPair(Currency.RUB, Currency.USD),
                        OrderDirection.BUY,
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(50)));
                orders.add(order);
            });

            Assertions.assertDoesNotThrow(() -> {
                Order order = orderService.createOrder(new OrderOperationDto(anotherClient,
                        new CurrencyPair(Currency.USD, Currency.RUB),
                        OrderDirection.SELL, BigDecimal.valueOf(8),
                        BigDecimal.valueOf(50)));
                orders.add(order);
            });
        }

        int expectedCountOrders = 10;
        Assertions.assertEquals(expectedCountOrders, orders.size());
    }



}
