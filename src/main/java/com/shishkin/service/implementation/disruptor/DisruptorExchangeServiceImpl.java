package com.shishkin.service.implementation.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.dto.OrderRequestDto;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderStatus;
import com.shishkin.service.AbstractExchangeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DisruptorExchangeServiceImpl extends AbstractExchangeService {
    private static final long ORDER_PROCESSING_TIMEOUT_MIN = 2;
    private static final int RING_BUFFER_SIZE = Double.valueOf(Math.pow(2, 16)).intValue();

    private final Disruptor<OrderRequestEventFactory> disruptor = new Disruptor<>(OrderRequestEventFactory.getInstanceFactory(),
    RING_BUFFER_SIZE,
    DaemonThreadFactory.INSTANCE);

    private final Map<CurrencyPair, OrderRequestEventHandler> orders = new HashMap<>();

    public DisruptorExchangeServiceImpl(Set<CurrencyPair> currencyPairs) {
        currencyPairs.forEach(currencyPair -> {
            OrderRequestEventHandler handler = new OrderRequestEventHandler(currencyPair);
            this.orders.put(currencyPair, handler);
            this.disruptor.handleEventsWith(handler);
        });
        this.disruptor.start();
    }

    @Override
    public void createOrder(OrderOperationDto orderOperationDto) {
        OrderRequestDto orderRequestDto = new OrderRequestDto(orderOperationDto);
        try {
            this.disruptor.getRingBuffer().publishEvent(((event, sequence) -> event.setOrderRequestDto(orderRequestDto)));
            if (!orderRequestDto.await(ORDER_PROCESSING_TIMEOUT_MIN, TimeUnit.MINUTES))
                throw new InterruptedException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Exchange was interrupted");
            this.disruptor.shutdown();
        }
    }

    @Override
    public List<Order> getOrders() {
        return orders.values()
                .stream()
                .flatMap(item -> item.getOrders().stream())
                .filter(OrderStatus::isActiveOrder)
                .toList();
    }
}
