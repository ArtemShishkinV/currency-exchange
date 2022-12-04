package com.shishkin.service.client;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.service.ClientService;
import com.shishkin.service.implementation.ClientServiceImpl;
import com.shishkin.utils.BigDecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ClientServiceAsyncTest {
    private static ClientService clientService;
    private CountDownLatch countDownLatch;

    @BeforeAll
    static void setup() {
        clientService = new ClientServiceImpl();
    }

    @BeforeEach
    void setupEach() {
        this.countDownLatch = new CountDownLatch(1);
    }


    @RepeatedTest(20)
    void deposit() throws InterruptedException {
        BigDecimal expected = BigDecimalUtils.round(BigDecimal.valueOf(50 * 10));
        ExecutorService executor = Executors.newCachedThreadPool();
        Client client = clientService.create();
        for (int i = 0; i < 50; i++) {
            executor.execute(() -> {
                try {
                    this.countDownLatch.await();
                    clientService.deposit(ClientOperationDto.create(client, Currency.EUR, 10));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        countDownLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);


        Assertions.assertEquals(expected, client.getAccounts().get(Currency.EUR));
    }

    @RepeatedTest(20)
    void withdraw() throws InterruptedException {
        BigDecimal deposit = BigDecimalUtils.round(BigDecimal.valueOf(50 * 10));

        ExecutorService executor = Executors.newCachedThreadPool();
        Client client = clientService.create();
        clientService.deposit(new ClientOperationDto(client, Currency.EUR, deposit));
        for (int i = 0; i < 50; i++) {
            executor.execute(() -> {
                try {
                    this.countDownLatch.await();
                    clientService.withdraw(ClientOperationDto.create(client, Currency.EUR, 10));
                } catch (InterruptedException | NotEnoughMoneyException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        countDownLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);


        Assertions.assertEquals(BigDecimalUtils.round(BigDecimal.ZERO), client.getAccounts().get(Currency.EUR));
    }

    @RepeatedTest(20)
    void withdrawWithException() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        Client client = clientService.create();
        clientService.deposit(new ClientOperationDto(client, Currency.EUR, BigDecimal.valueOf(50 * 10)));
        for (int i = 0; i < 50; i++) {
            executor.execute(() -> {
                try {
                    this.countDownLatch.await();
                    clientService.withdraw(ClientOperationDto.create(client, Currency.EUR, 11));
                } catch (InterruptedException | NotEnoughMoneyException e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        countDownLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);


        Assertions.assertEquals(BigDecimalUtils.round(BigDecimal.valueOf(5)), client.getAccounts().get(Currency.EUR));
    }


}
