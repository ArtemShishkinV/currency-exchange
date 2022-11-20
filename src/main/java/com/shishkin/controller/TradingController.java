package com.shishkin.controller;

import com.shishkin.model.Currency;
import com.shishkin.model.CurrencyPair;
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
    }
}
