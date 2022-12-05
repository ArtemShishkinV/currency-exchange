package com.shishkin.model.order;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class Order {
    private final Long id;
    private OrderStatus status;
    private final Client client;
    private final BigDecimal price;
    private BigDecimal amount;
    private final CurrencyPair currencyPair;
    private final OrderDirection orderDirection;
    private BigDecimal totalPrice;


    public Order(OrderOperationDto orderOperationDto) {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.client = orderOperationDto.getClient();
        this.price = orderOperationDto.getPrice();
        this.amount = orderOperationDto.getAmount();
        this.currencyPair = orderOperationDto.getCurrencyPair();
        this.status = OrderStatus.NEW;
        this.orderDirection = orderOperationDto.getOrderDirection();
        this.totalPrice = orderOperationDto.getTotalPrice();
    }
}
