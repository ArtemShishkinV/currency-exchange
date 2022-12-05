package com.shishkin.service;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.Client;

public interface ClientService {
    Client create();

    String getInfo(Client client);

    void deposit(ClientOperationDto clientOperationDto) throws IllegalArgumentException;

    void withdraw(ClientOperationDto clientOperationDto) throws NotEnoughMoneyException;

}
