package com.shishkin.dto;

import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class OrderOperationDto {
    Client client;
    CurrencyPair currencyPair;
    OrderDirection orderDirection;
    BigDecimal amount;
    BigDecimal price;
}
