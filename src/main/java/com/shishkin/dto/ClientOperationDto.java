package com.shishkin.dto;

import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.utils.BigDecimalUtils;

import java.math.BigDecimal;

public record ClientOperationDto(Client client,
                                 Currency currency,
                                 BigDecimal amount) {

    public ClientOperationDto(Client client, Currency currency, BigDecimal amount) {
        this.client = client;
        this.currency = currency;
        this.amount = BigDecimalUtils.round(amount);
    }

    public static ClientOperationDto create(Client client, Currency currency, int amount) {
        return new ClientOperationDto(client, currency, BigDecimal.valueOf(amount));
    }
}
