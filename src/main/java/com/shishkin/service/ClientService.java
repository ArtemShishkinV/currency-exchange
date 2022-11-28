package com.shishkin.service;

import com.shishkin.model.Client;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientService {
    public static String getInfo(Client client) {
        return "Client with id " + client.getId() + "\n" + client.getAccounts().entrySet()
                .stream()
                .map(item -> item.getKey() + " : " + item.getValue())
                .collect(Collectors.joining("\n"));
    }
}
