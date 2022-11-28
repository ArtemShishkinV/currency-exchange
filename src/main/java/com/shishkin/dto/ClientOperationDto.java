package com.shishkin.dto;

import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ClientOperationDto {
    Client client;
    Currency currency;
    BigDecimal amount;
}
