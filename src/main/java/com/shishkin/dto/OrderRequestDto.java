package com.shishkin.dto;

import com.shishkin.model.order.OrderStatus;

import java.util.concurrent.CountDownLatch;

public class OrderRequestDto {
    private final CountDownLatch completedLatch = new CountDownLatch(1);
    private final OrderOperationDto orderOperationDto;
    private OrderStatus orderStatus;

    public OrderRequestDto(OrderOperationDto orderOperationDto) {
        this.orderOperationDto = orderOperationDto;
    }

    public OrderOperationDto getOrderOperationDto() {
        return orderOperationDto;
    }

    public void setOrderOperationDto(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void complete() {
        this.completedLatch.countDown();
    }
}
