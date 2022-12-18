package com.shishkin.service.exchange;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.service.ExchangeService;
import com.shishkin.service.OrderService;
import com.shishkin.service.implementation.queue.QueueExchangeServiceImpl;
import com.shishkin.service.implementation.simple.OrderServiceImpl;
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

    private static OrderService orderService;

    private static Client client;
    private static Client anotherClient;

    @BeforeAll
    static void setup() {
        orderService = new OrderServiceImpl();
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
    void testMatchOrdersFillTwoWhenBuyPriceEqualSellPrice() {
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

    @Test
    void testMatchOrdersFillTwoWhenBuyPriceMoreSellPrice() {
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
                BigDecimal.valueOf(45)));

        BigDecimal expectedUsdFirstClient = BigDecimalUtils.round(BigDecimal.TEN);
        BigDecimal expectedRubFirstClient = BigDecimalUtils.round(BigDecimal.valueOf(50));
        BigDecimal expectedUsdSecondClient = BigDecimalUtils.round(BigDecimal.ZERO);
        BigDecimal expectedRubSecondClient = BigDecimalUtils.round(BigDecimal.valueOf(450));

        Assertions.assertEquals(expectedUsdFirstClient, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubFirstClient, client.getAccounts().get(Currency.RUB));
        Assertions.assertEquals(expectedUsdSecondClient, anotherClient.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClient, anotherClient.getAccounts().get(Currency.RUB));
    }

    @Test
    void testMatchOrdersFillAndPartiallyBuyPriceEqualSellPrice() {
        exchangeService.createOrder(new OrderOperationDto(
                client,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.BUY,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50)));

        exchangeService.createOrder(new OrderOperationDto(
                anotherClient,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.SELL,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(45)));

        BigDecimal expectedUsdFirstClientBeforeRevoke = BigDecimalUtils.round(BigDecimal.valueOf(8));
        BigDecimal expectedRubFirstClientBeforeRevoke = BigDecimalUtils.round(BigDecimal.valueOf(140));
        BigDecimal expectedUsdSecondClientBeforeRevoke = BigDecimalUtils.round(BigDecimal.ZERO);
        BigDecimal expectedRubSecondClientBeforeRevoke = BigDecimalUtils.round(BigDecimal.valueOf(360));

        BigDecimal expectedUsdSecondClientAfterRevoke = BigDecimalUtils.round(BigDecimal.valueOf(2));


        Assertions.assertEquals(expectedUsdFirstClientBeforeRevoke, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubFirstClientBeforeRevoke, client.getAccounts().get(Currency.RUB));
        Assertions.assertEquals(expectedUsdSecondClientBeforeRevoke, anotherClient.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClientBeforeRevoke, anotherClient.getAccounts().get(Currency.RUB));


        exchangeService.getOrders().forEach(orderService::revoke);

        Assertions.assertEquals(expectedUsdFirstClientBeforeRevoke, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubFirstClientBeforeRevoke, client.getAccounts().get(Currency.RUB));
        Assertions.assertEquals(expectedUsdSecondClientAfterRevoke, anotherClient.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClientBeforeRevoke, anotherClient.getAccounts().get(Currency.RUB));
    }

    @Test
    void testMatchOrdersPartiallyTwoOrdersAndFillAfter() {
        exchangeService.createOrder(new OrderOperationDto(
                client,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.BUY,
                BigDecimal.valueOf(7),
                BigDecimal.valueOf(52)));

        exchangeService.createOrder(new OrderOperationDto(
                anotherClient,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.SELL,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(45)));

        BigDecimal expectedUsdFirstClientAfterFirstMatch = BigDecimalUtils.round(BigDecimal.valueOf(7));
        BigDecimal expectedRubFirstClientAfterFirstMatch = BigDecimalUtils.round(BigDecimal.valueOf(185));
        BigDecimal expectedUsdSecondClientAfterFirstMatch = BigDecimalUtils.round(BigDecimal.ZERO);
        BigDecimal expectedRubSecondClientAfterFirstMatch = BigDecimalUtils.round(BigDecimal.valueOf(315));

        Assertions.assertEquals(expectedUsdFirstClientAfterFirstMatch, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubFirstClientAfterFirstMatch, client.getAccounts().get(Currency.RUB));
        Assertions.assertEquals(expectedUsdSecondClientAfterFirstMatch, anotherClient.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClientAfterFirstMatch, anotherClient.getAccounts().get(Currency.RUB));

        exchangeService.createOrder(new OrderOperationDto(
                client,
                new CurrencyPair(Currency.USD, Currency.RUB),
                OrderDirection.BUY,
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(45)));

        BigDecimal expectedUsdFirstClientAfterSecondMatch = BigDecimalUtils.round(BigDecimal.valueOf(10));
        BigDecimal expectedRubFirstClientAfterSecondMatch = BigDecimalUtils.round(BigDecimal.valueOf(5));
        BigDecimal expectedUsdSecondClientAfterSecondMatch = BigDecimalUtils.round(BigDecimal.ZERO);
        BigDecimal expectedRubSecondClientAfterSecondMatch = BigDecimalUtils.round(BigDecimal.valueOf(450));

        Assertions.assertEquals(expectedUsdFirstClientAfterSecondMatch, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubFirstClientAfterSecondMatch, client.getAccounts().get(Currency.RUB));
        Assertions.assertEquals(expectedUsdSecondClientAfterSecondMatch, anotherClient.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClientAfterSecondMatch, anotherClient.getAccounts().get(Currency.RUB));


        exchangeService.getOrders().forEach(orderService::revoke);

        BigDecimal expectedRubFirstClientAfterRevoke = BigDecimalUtils.round(BigDecimal.valueOf(50));

        Assertions.assertEquals(expectedUsdFirstClientAfterSecondMatch, client.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubFirstClientAfterRevoke, client.getAccounts().get(Currency.RUB));
        Assertions.assertEquals(expectedUsdSecondClientAfterSecondMatch, anotherClient.getAccounts().get(Currency.USD));
        Assertions.assertEquals(expectedRubSecondClientAfterSecondMatch, anotherClient.getAccounts().get(Currency.RUB));

    }

}
