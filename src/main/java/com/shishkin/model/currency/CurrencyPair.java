package com.shishkin.model.currency;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrencyPair {
    Currency from;
    Currency to;
    double price;
}
