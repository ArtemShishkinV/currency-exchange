package com.shishkin.service.client;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.model.currency.Currency;
import com.shishkin.service.ClientService;
import com.shishkin.service.implementation.simple.ClientServiceImpl;
import com.shishkin.utils.BigDecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

class ClientServiceTest {
    private static ClientService clientService;
    private static Client client;

    @BeforeAll
    static void setup() {
        clientService = new ClientServiceImpl();
    }

    @BeforeEach
    void setupEach() {
        client = clientService.create();
    }

    @Test
    void createClient() {
        Client client = clientService.create();
        Assertions.assertNotNull(client);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "EUR, 10, 10",
            "USD, 11, 11",
            "RUB, 5.0012, 5.0012",
            "CNY, 7.11, 7.11"
    })
    void depositSuccess(String curr, BigDecimal dep, BigDecimal expected) {
        Currency currency = Currency.valueOf(curr);
        expected = BigDecimalUtils.round(expected);
        clientService.deposit(new ClientOperationDto(client, currency, dep));
        Assertions.assertEquals(expected, client.getAccounts().get(currency));
    }

    @ParameterizedTest()
    @CsvSource(value = {
            "EUR, 10, 10",
            "USD, 11, 11",
            "RUB, 7, 5.0012",
            "CNY, 7.1002, 7.1001"
    })
    void withdrawSuccess(String curr, BigDecimal dep, BigDecimal withdraw) {
        Currency currency = Currency.valueOf(curr);
        BigDecimal expected = BigDecimalUtils.round(dep.subtract(withdraw));
        clientService.deposit(new ClientOperationDto(client, currency, dep));

        Assertions.assertDoesNotThrow(() -> clientService.withdraw(new ClientOperationDto(client, currency, withdraw)));
        Assertions.assertEquals(expected, client.getAccounts().get(currency));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "EUR, 10",
            "USD, 11",
    })
    void withdrawNotEnoughMoney(String curr, Double amount) {
        Currency currency = Currency.valueOf(curr);

        Assertions.assertThrows(NotEnoughMoneyException.class,
                () -> clientService.withdraw(new ClientOperationDto(client, currency, BigDecimal.valueOf(amount))));
        Assertions.assertEquals(BigDecimalUtils.round(BigDecimal.ZERO), client.getAccounts().get(currency));
    }

}