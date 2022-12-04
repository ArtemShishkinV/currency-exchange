package com.shishkin.service;

import com.shishkin.model.Client;
import com.shishkin.service.implementation.ClientServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientServiceTest {
    private static ClientService clientService;

    @BeforeAll
    static void setup() {
        clientService = new ClientServiceImpl();
    }

    @Test
    void createClient() {
        Client client = clientService.create();
        Assertions.assertNotNull(client);
    }

    @Test
    void depositSuccess() {

    }

    @Test
    void withdraw() {
    }

    @Test
    void depositAndWithdraw() {

    }
}