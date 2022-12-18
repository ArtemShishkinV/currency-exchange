package com.shishkin.service.exchange;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.implementation.queue.QueueExchangeServiceImpl;
import com.shishkin.utils.BigDecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

class ExchangeServiceTest {
    private static final Set<CurrencyPair> CURRENCY_PAIRS = Set.of(
            new CurrencyPair(Currency.USD, Currency.RUB)
    );
    private static ExchangeService exchangeService;

    private static Client client;
    private static Client anotherClient;

    @BeforeAll
    static void setup() {
        exchangeService = new QueueExchangeServiceImpl(CURRENCY_PAIRS);
    }

    @BeforeEach
    void setupEach() {
        client = exchangeService.createClient();
        exchangeService.deposit(ClientOperationDto.create(client, Currency.RUB, 500));
        anotherClient = exchangeService.createClient();
        exchangeService.deposit(ClientOperationDto.create(anotherClient, Currency.USD, 10));
    }

    @Test
    void matchAndFillTwoOrders() {
        exchangeService.createOrder(new OrderOperationDto(
                client,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.BUY,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(50)));

        exchangeService.createOrder(new OrderOperationDto(
                anotherClient,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.SELL,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(50)));
        BigDecimal expectedUsdFirstClient = BigDecimalUtils.round(BigDecimal.TEN);
        BigDecimal expectedRubSecondClient = BigDecimalUtils.round(BigDecimal.valueOf(500));

        Assertions.assertEquals(expectedUsdFirstClient, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClient, anotherClient.getAccounts().get(Currency.RUB));
    }


}
