package com.shishkin.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyPair {
    Currency from;
    Currency to;
    double price;
    String ticker;

    public CurrencyPair(Currency from, Currency to, double price) {
        this.from = from;
        this.to = to;
        this.price = price;
        this.ticker = from.name() + to.name();
    }
}
