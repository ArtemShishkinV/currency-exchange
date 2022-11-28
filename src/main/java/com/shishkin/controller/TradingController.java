package com.shishkin.controller;

import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.service.ClientService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TradingController {
    private static final Set<CurrencyPair> pairs = Set.of(
            new CurrencyPair(Currency.RUB, Currency.USD, 0.1655),
            new CurrencyPair(Currency.EUR, Currency.USD, 1.0323)
    );

    public static void start() {
        pairs.forEach(System.out::println);
        Client client = new Client();
        System.out.println(ClientService.getInfo(client));
    }
}
