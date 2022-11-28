package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.model.Client;
import com.shishkin.service.ClientService;

import java.util.stream.Collectors;

public class ClientServiceImpl implements ClientService {
    @Override
    public void deposit(ClientOperationDto clientOperationDto) {

    }

    @Override
    public Client create() {
        return new Client();
    }

    @Override
    public void withdraw(ClientOperationDto clientOperationDto) {

    }

    public String getInfo(Client client) {
        return "Client with id " + client.getId() + "\n" + client.getAccounts().entrySet()
                .stream()
                .map(item -> item.getKey() + " : " + item.getValue())
                .collect(Collectors.joining("\n"));
    }
}
