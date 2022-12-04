package com.shishkin.service.implementation;

import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.order.LimitOrder;
import com.shishkin.model.order.Order;
import com.shishkin.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    @Override
    public Order createOrder(OrderOperationDto orderOperationDto) throws NotEnoughMoneyException {
        switch (orderOperationDto.orderDirection()) {
            case BUY -> {

            }
            case SELL -> {
                BigDecimal balance = orderOperationDto.client().getAccounts()
                        .get(orderOperationDto.currencyPair().getFrom());
                BigDecimal needMoney = orderOperationDto.amount().multiply(orderOperationDto.price());
                if (needMoney.compareTo(balance) > 0) {
                    throw new NotEnoughMoneyException(
                            String.format("Client id: %d, not enough %s for create order; required: %f; access: %f;",
                                    orderOperationDto.client().getId(),
                                    orderOperationDto.currencyPair().getFrom(),
                                    needMoney,
                                    balance));
                }
            }
        }
        return new LimitOrder(orderOperationDto);
    }

    @Override
    public List<Order> getOrders(Currency currency) {
        return null;
    }

    @Override
    public List<Order> getAllOrders() {
        return null;
    }

    private void checkMoneyToCreateOrder(OrderOperationDto orderOperationDto, Currency currency)
            throws NotEnoughMoneyException {
        BigDecimal balance = orderOperationDto.client().getAccounts()
                .get(currency);
        BigDecimal needMoney = orderOperationDto.amount().multiply(orderOperationDto.price());
        if (needMoney.compareTo(balance) > 0) {
            throw new NotEnoughMoneyException(
                    String.format("Client id: %d, not enough %s for create order; required: %f; access: %f;",
                            orderOperationDto.client().getId(),
                            orderOperationDto.currencyPair().getTo(),
                            needMoney,
                            balance));
        }
    }
}
