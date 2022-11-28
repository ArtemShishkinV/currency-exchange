package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;
import com.shishkin.service.ClientService;

import java.util.stream.Collectors;

public class ClientServiceImpl implements ClientService {
    @Override
    public Client create() {
        return new Client();
    }

    @Override
    public void deposit(ClientOperationDto clientOperationDto) throws IllegalArgumentException {
        Client client = clientOperationDto.getClient();
        client.deposit(clientOperationDto.getCurrency(), clientOperationDto.getAmount());
    }

    @Override
    public void withdraw(ClientOperationDto clientOperationDto) throws NotEnoughMoneyException {
        Client client = clientOperationDto.getClient();
        client.withdraw(clientOperationDto.getCurrency(), clientOperationDto.getAmount());
    }

    public String getInfo(Client client) {
        return "Client with id " + client.getId() + "\n" + client.getAccounts().entrySet()
                .stream()
                .map(item -> item.getKey() + " : " + item.getValue())
                .collect(Collectors.joining("\n"));
    }
}
