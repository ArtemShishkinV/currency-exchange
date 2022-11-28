package com.shishkin.service;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.model.Client;

public interface ClientService {
    Client create();

    String getInfo(Client client);

    void deposit(ClientOperationDto clientOperationDto);

    void withdraw(ClientOperationDto clientOperationDto);
}