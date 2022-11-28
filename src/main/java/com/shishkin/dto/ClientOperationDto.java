package com.shishkin.dto;

import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.utils.BigDecimalUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ClientOperationDto {
    Client client;
    Currency currency;
    BigDecimal amount;

    public ClientOperationDto(Client client, Currency currency, BigDecimal amount) {
        this.client = client;
        this.currency = currency;
        this.amount = BigDecimalUtils.round(amount);
    }

    public ClientOperationDto(Client client, Currency currency, int amount) {
        this.client = client;
        this.currency = currency;
        this.amount = BigDecimalUtils.round(new BigDecimal(amount));
    }
}
