package com.shishkin.service.implementation.queue;

import com.shishkin.dto.OrderRequestDto;
import com.shishkin.model.currency.CurrencyPair;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class OrderRequestListener implements Runnable {
    private static final int TIMEOUT = 5;
    private final BlockingQueue<OrderRequestDto> orderRequests;
    private final Map<CurrencyPair, BlockingQueue<OrderRequestDto>> blockingQueueMap;

    public OrderRequestListener(BlockingQueue<OrderRequestDto> orderRequests,
                                Map<CurrencyPair, BlockingQueue<OrderRequestDto>> blockingQueueMap) {
        this.orderRequests = orderRequests;
        this.blockingQueueMap = blockingQueueMap;
    }

    @Override
    public void run() {
        try {
            registerOrder();
        } catch (InterruptedException e) {
            System.err.println("Exchange was interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    public void registerOrder() throws InterruptedException {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            OrderRequestDto orderRequestDto = orderRequests.poll(TIMEOUT, TimeUnit.MINUTES);
            if (orderRequestDto != null) {
                blockingQueueMap.get(orderRequestDto.getOrderOperationDto().getCurrencyPair())
                        .add(orderRequestDto);
            }

        }
    }
}
