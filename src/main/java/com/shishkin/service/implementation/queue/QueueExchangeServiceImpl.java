package com.shishkin.service.implementation.queue;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.dto.OrderRequestDto;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.service.AbstractExchangeService;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    }

    private void createCurrencyPairsThreads(Set<CurrencyPair> currencyPairs) {
        for (CurrencyPair currencyPair:
             currencyPairs) {
            Thread thread = new Thread(new CurrencyOrderRequestListener(currencyPair, requestOrders, orders));
            currencyPairRequestsListeners.put(currencyPair, thread);
        }
    }

    @Override
    public void createOrder(OrderOperationDto orderOperationDto) {

    }

    @Override
    public List<Order> getOrders() {
        return null;
    }

    private void initCurrencyPairs(Set<CurrencyPair> currencyPairs) {
        for (CurrencyPair currencyPair : currencyPairs) {
            orders.put(currencyPair, new ArrayList<>());
        }
    }

    private void createRequestThread() {
        Thread requestOrdersListener = new Thread(new OrderRequestListener(requestOrders, currencyPairRequestOrders));
        requestOrdersListener.setDaemon(true);

        requestOrdersListener.start();
    }
}
