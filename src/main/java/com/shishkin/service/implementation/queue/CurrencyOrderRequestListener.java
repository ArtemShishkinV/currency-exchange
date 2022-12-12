package com.shishkin.service.implementation.queue;

import com.shishkin.dto.OrderRequestDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.simple.OrderServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CurrencyOrderRequestListener implements Runnable{
    private static final int TIMEOUT = 5;

    private static final OrderService ORDER_SERVICE = new OrderServiceImpl();

    private final CurrencyPair currencyPair;

    private final BlockingQueue<OrderRequestDto> orderRequests;

    private final Map<CurrencyPair, List<Order>> orders;

    public CurrencyOrderRequestListener(CurrencyPair currencyPair,
                                        BlockingQueue<OrderRequestDto> orderRequests,
                                        Map<CurrencyPair, List<Order>> orders) {
        this.currencyPair = currencyPair;
        this.orderRequests = orderRequests;
        this.orders = orders;
    }

    @Override
    public void run() {
        try {
            createOrder();
        } catch (InterruptedException e) {
            System.err.printf("%s listener was interrupted%n", currencyPair);
            Thread.currentThread().interrupt();
        }
    }

    private void createOrder() throws InterruptedException {
        while(true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            OrderRequestDto orderRequest = this.orderRequests.poll(TIMEOUT, TimeUnit.MINUTES);
            if (orderRequest != null) {
                getOrder(orderRequest);
            }
        }
    }

    private void getOrder(OrderRequestDto orderRequestDto) {
        try {
            Order order = ORDER_SERVICE.createOrder(orderRequestDto.getOrderOperationDto());
            ORDER_SERVICE.processOrder(order, orders.get(orderRequestDto.getOrderOperationDto().getCurrencyPair()));
            orderRequestDto.complete();
        } catch (NotEnoughMoneyException e) {
            System.err.println(e.getMessage());
            orderRequestDto.complete();
        }
    }
}
