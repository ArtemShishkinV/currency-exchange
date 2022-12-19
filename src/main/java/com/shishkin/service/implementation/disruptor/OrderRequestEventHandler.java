package com.shishkin.service.implementation.disruptor;

import com.lmax.disruptor.EventHandler;
import com.shishkin.dto.OrderRequestDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.simple.OrderServiceImpl;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OrderRequestEventHandler implements EventHandler<OrderRequestEventFactory> {
    private static final int INIT_CAPACITY_ARRAY_LIST = 5_000;
    private static final OrderService ORDER_SERVICE = new OrderServiceImpl();

    private final CurrencyPair currencyPair;
    private final List<Order> orders;

    public OrderRequestEventHandler(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        this.orders = new ArrayList<>(INIT_CAPACITY_ARRAY_LIST);
    }

    @Override
    public void onEvent(OrderRequestEventFactory event, long sequence, boolean endOfBatch) throws Exception {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        OrderRequestDto orderRequestDto = event.getOrderRequestDto();
        try {
            System.out.println("work");
            Order order = ORDER_SERVICE.createOrder(orderRequestDto.getOrderOperationDto());
            ORDER_SERVICE.processOrder(order, orders);
            orderRequestDto.complete();
        } catch (NotEnoughMoneyException e) {
            System.err.println(e.getMessage());
            orderRequestDto.complete();
        }
    }
}
