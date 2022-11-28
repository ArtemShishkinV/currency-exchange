package com.shishkin.model.order;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public abstract class Order {
    private final Long id;
    private final Client client;
    private final BigDecimal price;
    private BigDecimal amount;
    private CurrencyPair currencyPair;

    protected Order(OrderOperationDto orderOperationDto) {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.client = orderOperationDto.getClient();
        this.price = orderOperationDto.getPrice();
        this.amount = orderOperationDto.getAmount();
    }
}
