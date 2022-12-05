package com.shishkin.dto;

import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderOperationDto {
    private final Client client;
    private final CurrencyPair currencyPair;
    private final OrderDirection orderDirection;
    private final BigDecimal amount;
    private final BigDecimal price;
    private final BigDecimal totalPrice;

    public OrderOperationDto(Client client, CurrencyPair currencyPair,
                             OrderDirection orderDirection, BigDecimal amount, BigDecimal price) {
        this.client = client;
        this.currencyPair = currencyPair;
        this.orderDirection = orderDirection;
        this.amount = amount;
        this.price = price;
        if (OrderDirection.BUY.equals(orderDirection)) {
            this.totalPrice = amount.multiply(price);
        } else {
            this.totalPrice = amount;
        }
    }
}
