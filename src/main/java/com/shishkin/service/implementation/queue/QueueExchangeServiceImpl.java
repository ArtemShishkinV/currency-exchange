package com.shishkin.service.implementation.queue;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.dto.OrderRequestDto;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.service.AbstractExchangeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueExchangeServiceImpl extends AbstractExchangeService {
    private static final long ORDER_PROCESSING_TIMEOUT_MIN = 2;

    private final BlockingQueue<OrderRequestDto> requestOrders = new LinkedBlockingQueue<>();

    private final Map<CurrencyPair, BlockingQueue<OrderRequestDto>> currencyPairRequestOrders = new HashMap<>();
    private final Map<CurrencyPair, List<Order>> orders = new HashMap<>();
    private final Map<CurrencyPair, Thread> currencyPairRequestsListeners = new HashMap<>();

    public QueueExchangeServiceImpl(Set<CurrencyPair> currencyPairs) {
        initCurrencyPairs(currencyPairs);
        createRequestThread();
        createCurrencyPairsThreads(currencyPairs);
        currencyPairRequestsListeners.values().forEach(Thread::start);
    }

    private void createCurrencyPairsThreads(Set<CurrencyPair> currencyPairs) {
        for (CurrencyPair currencyPair :
                currencyPairs) {
            Thread thread = new Thread(new CurrencyOrderRequestListener(currencyPair, requestOrders, orders));
            thread.setDaemon(true);

            currencyPairRequestsListeners.put(currencyPair, thread);
        }
        for (Map.Entry<CurrencyPair, BlockingQueue<OrderRequestDto>> entry : currencyPairRequestOrders.entrySet()) {
            CurrencyPair pair = entry.getKey();
            Thread listenerThread = new Thread(new CurrencyOrderRequestListener(pair, entry.getValue(), orders));
            listenerThread.setDaemon(true);
            currencyPairRequestsListeners.put(pair, listenerThread);
        }

    }

    @Override
    public void createOrder(OrderOperationDto orderOperationDto) {
        OrderRequestDto orderRequestDto = new OrderRequestDto(orderOperationDto);
        requestOrders.add(orderRequestDto);
        try {
            if (!orderRequestDto.await()) throw new InterruptedException();
        } catch (InterruptedException e) {
            System.err.println("Exchange was interrupted");
        }

    }

    @Override
    public List<Order> getOrders() {
        return ORDER_SERVICE.getActiveOrders(Collections.unmodifiableMap(orders));
    }

    private void initCurrencyPairs(Set<CurrencyPair> currencyPairs) {
        for (CurrencyPair currencyPair : currencyPairs) {
            orders.put(currencyPair, new ArrayList<>());
            currencyPairRequestOrders.put(currencyPair, new LinkedBlockingQueue<>());
        }
    }

    private void createRequestThread() {
        Thread requestOrdersListener = new Thread(new OrderRequestListener(requestOrders, currencyPairRequestOrders));
        requestOrdersListener.setDaemon(true);

        requestOrdersListener.start();
    }
}
