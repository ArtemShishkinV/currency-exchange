package com.shishkin.dto;

import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;

import java.math.BigDecimal;

public record OrderOperationDto(Client client,
                         CurrencyPair currencyPair,
                         OrderDirection orderDirection,
                         BigDecimal amount,
                         BigDecimal price) {
}
