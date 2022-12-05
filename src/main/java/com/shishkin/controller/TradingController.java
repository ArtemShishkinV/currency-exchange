package com.shishkin.controller;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.ExchangeServiceImpl;
import com.shishkin.service.implementation.OrderServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TradingController {
    private static final Set<CurrencyPair> CURRENCY_PAIRS = Set.of(
            new CurrencyPair(Currency.RUB, Currency.USD)
    );
    private static final ExchangeService exchangeService = new ExchangeServiceImpl(CURRENCY_PAIRS);


    public static void start() {
        Client client = exchangeService.createClient();
        Client anotherClient = exchangeService.createClient();
        exchangeService.getInfo(client);
        exchangeService.deposit(ClientOperationDto.create(client, Currency.RUB, 350));
        exchangeService.deposit(ClientOperationDto.create(anotherClient, Currency.USD, 7));
        exchangeService.getInfo(client);
        exchangeService.getInfo(anotherClient);

        exchangeService.createOrder(new OrderOperationDto(
                client,
                new CurrencyPair(Currency.RUB, Currency.USD),
                OrderDirection.BUY,
                BigDecimal.valueOf(7),
                BigDecimal.valueOf(50)));

        exchangeService.createOrder(new OrderOperationDto(
                anotherClient,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.SELL,
                BigDecimal.valueOf(7),
                BigDecimal.valueOf(50)));

        exchangeService.getInfo(client);
        exchangeService.getInfo(anotherClient);
    }
}
