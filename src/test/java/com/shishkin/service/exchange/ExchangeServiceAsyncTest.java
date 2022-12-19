package com.shishkin.service.exchange;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.disruptor.DisruptorExchangeServiceImpl;
import com.shishkin.service.implementation.queue.QueueExchangeServiceImpl;
import com.shishkin.service.implementation.simple.OrderServiceImpl;
import com.shishkin.utils.BigDecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

class ExchangeServiceAsyncTest {
    private static final int COUNT_ORDERS_CLIENT = 100;
    private static final int COUNT_CLIENTS = 50;
    private static final Set<CurrencyPair> CURRENCY_PAIRS = Set.of(
            new CurrencyPair(Currency.RUB, Currency.USD),
            new CurrencyPair(Currency.RUB, Currency.EUR),
            new CurrencyPair(Currency.RUB, Currency.CNY),
            new CurrencyPair(Currency.USD, Currency.EUR),
            new CurrencyPair(Currency.USD, Currency.CNY),
            new CurrencyPair(Currency.EUR, Currency.CNY)
    );
    private static ExchangeService exchangeService;
    private CountDownLatch countDownLatch;

    @BeforeAll
    static void setup() {
        exchangeService = new QueueExchangeServiceImpl(CURRENCY_PAIRS);
    }

    @BeforeEach
    void setupEach() {
        this.countDownLatch = new CountDownLatch(1);
    }

    @RepeatedTest(20)
    void sumAfterManyRandomOrdersTrading() throws InterruptedException {
        List<Client> clients = new ArrayList<>();
        OrderService orderService = new OrderServiceImpl();
        BigDecimal deposit = BigDecimalUtils.round(BigDecimal.valueOf(1_000_000));
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < COUNT_CLIENTS; i++) {
            Client client = exchangeService.createClient();
            exchangeService.deposit(new ClientOperationDto(client, Currency.USD, deposit));
            exchangeService.deposit(new ClientOperationDto(client, Currency.EUR, deposit));
            exchangeService.deposit(new ClientOperationDto(client, Currency.CNY, deposit));
            exchangeService.deposit(new ClientOperationDto(client, Currency.RUB, deposit));
            clients.add(client);
        }

        BigDecimal expected = BigDecimalUtils.round(deposit.multiply(
                BigDecimal.valueOf(COUNT_CLIENTS)).multiply(BigDecimal.valueOf(4)));

        clients.forEach(client -> {
            for (int i = 0; i < COUNT_ORDERS_CLIENT; i++) {
                executor.execute(() -> {
                    try {
                        this.countDownLatch.await();
                        exchangeService.createOrder(getRandomOrder(client));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });

        this.countDownLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);

        Thread.sleep(1000);

        System.out.println("end");

        for (Order order : exchangeService.getOrders()) orderService.revoke(order);


        BigDecimal sum = BigDecimalUtils.round(BigDecimal.ZERO);


        for (Client client : clients) {
            Map<Currency, BigDecimal> clientAccs = client.getAccounts();
            sum = sum.add(clientAccs.get(Currency.RUB)
                    .add(clientAccs.get(Currency.USD)
                            .add(clientAccs.get(Currency.EUR)
                                    .add(clientAccs.get(Currency.CNY)))));
        }

        Assertions.assertEquals(expected, sum);
    }

    private OrderOperationDto getRandomOrder(Client client) {
        BigDecimal amount = BigDecimalUtils.round(BigDecimal.valueOf(
                ThreadLocalRandom.current().nextInt(10, 100)));
        BigDecimal price = BigDecimalUtils.round(BigDecimal.valueOf(
                ThreadLocalRandom.current().nextInt(10, 50)));
        return new OrderOperationDto(client, getRandomCurrencyPair(), getRandomOrderDirection(), amount, price);
    }

    private CurrencyPair getRandomCurrencyPair() {
        int size = CURRENCY_PAIRS.size();
        int item = ThreadLocalRandom.current().nextInt(size);
        int i = 0;
        CurrencyPair currencyPair = null;
        for (CurrencyPair obj : CURRENCY_PAIRS) {
            if (i == item)
                currencyPair = obj;
            i++;
        }
        return currencyPair;
    }

    private OrderDirection getRandomOrderDirection() {
        return switch (ThreadLocalRandom.current().nextInt(0, 2)) {
            case 1 -> OrderDirection.BUY;
            default -> OrderDirection.SELL;
        };
    }

}
