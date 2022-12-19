package com.shishkin.dto;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OrderRequestDto {
    private final CountDownLatch completedLatch = new CountDownLatch(1);
    private final OrderOperationDto orderOperationDto;

    public OrderRequestDto(OrderOperationDto orderOperationDto) {
        this.orderOperationDto = orderOperationDto;
    }

    public OrderOperationDto getOrderOperationDto() {
        return orderOperationDto;
    }


    public boolean await(long timeoutValue, TimeUnit unit) throws InterruptedException {
        return this.completedLatch.await(timeoutValue, unit);
    }

    public void complete() {
        this.completedLatch.countDown();
    }
}
