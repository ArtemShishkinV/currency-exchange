package com.shishkin.controller;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.simple.ExchangeServiceImpl;
import com.shishkin.service.implementation.simple.OrderServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TradingController {
    private static final Set<CurrencyPair> CURRENCY_PAIRS = Set.of(
            new CurrencyPair(Currency.RUB, Currency.USD)
    );
    private static final ExchangeService EXCHANGE_SERVICE = new ExchangeServiceImpl(CURRENCY_PAIRS);
    private static final OrderService ORDER_SERVICE = new OrderServiceImpl();

    public static void start() {
        Client client = EXCHANGE_SERVICE.createClient();
        Client anotherClient = EXCHANGE_SERVICE.createClient();
        EXCHANGE_SERVICE.getInfo(client);
        EXCHANGE_SERVICE.deposit(ClientOperationDto.create(client, Currency.RUB, 350));
        EXCHANGE_SERVICE.deposit(ClientOperationDto.create(anotherClient, Currency.USD, 10));

        System.out.println(EXCHANGE_SERVICE.getInfo(client));
        System.out.println(EXCHANGE_SERVICE.getInfo(anotherClient));

        EXCHANGE_SERVICE.createOrder(new OrderOperationDto(
                client,
                new CurrencyPair(Currency.RUB, Currency.USD),
                OrderDirection.BUY,
                BigDecimal.valueOf(7),
                BigDecimal.valueOf(50)));

        EXCHANGE_SERVICE.createOrder(new OrderOperationDto(
                anotherClient,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.SELL,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(42)));

        EXCHANGE_SERVICE.getOrders().forEach(ORDER_SERVICE::revoke);
        System.out.println(EXCHANGE_SERVICE.getInfo(client));
        System.out.println(EXCHANGE_SERVICE.getInfo(anotherClient));
        System.out.println(EXCHANGE_SERVICE.getOrders());
    }
}
