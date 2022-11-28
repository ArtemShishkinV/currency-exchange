package com.shishkin.model.order;

import com.shishkin.dto.OrderOperationDto;

public class LimitOrder extends Order{
    private final OrderDirection orderDirection;

    public LimitOrder(OrderOperationDto orderOperationDto) {
        super(orderOperationDto);
        this.orderDirection = orderOperationDto.getOrderDirection();
    }
}
