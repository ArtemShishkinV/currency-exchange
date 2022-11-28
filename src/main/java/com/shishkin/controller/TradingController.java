package com.shishkin.controller;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.implementation.ExchangeServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TradingController {
    private static final Set<CurrencyPair> CURRENCY_PAIRS = Set.of(
            new CurrencyPair(Currency.RUB, Currency.USD, 0.1655),
            new CurrencyPair(Currency.EUR, Currency.USD, 1.0323)
    );
    private static final ExchangeService exchangeService = new ExchangeServiceImpl(CURRENCY_PAIRS);


    public static void start() {
        Client client = exchangeService.createClient();
        exchangeService.getInfo(client);
        exchangeService.deposit(new ClientOperationDto(client, Currency.RUB, 110));
        exchangeService.getInfo(client);
        exchangeService.withdraw((new ClientOperationDto(client, Currency.RUB, BigDecimal.valueOf(110.0001))));
        exchangeService.getInfo(client);
    }
}
