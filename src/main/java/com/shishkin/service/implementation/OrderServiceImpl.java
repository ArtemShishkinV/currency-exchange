package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.model.order.OrderStatus;
import com.shishkin.service.ClientService;
import com.shishkin.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    private static ClientService clientService = new ClientServiceImpl();

    @Override
    public Order createOrder(OrderOperationDto orderOperationDto) throws NotEnoughMoneyException {
        checkMoneyToCreateOrder(orderOperationDto);
        //TODO: add withdraw client money with transaction and rollback if error
        return new Order(orderOperationDto);
    }

    @Override
    public void execute(Order order, Order anotherOrder) {
        if (order.getOrderDirection().equals(OrderDirection.BUY)
                && anotherOrder.getOrderDirection().equals(OrderDirection.SELL)) {
            executeOrders(order, anotherOrder);
        } else {
            executeOrders(anotherOrder, order);
        }
    }

    private void executeOrders(Order buyOrder,
                               Order sellOrder) {
        if (buyOrder.getTotalPrice().compareTo(sellOrder.getTotalPrice().multiply(buyOrder.getPrice())) == 0) {
            fillOrders(buyOrder, sellOrder);
        } else {
            partiallyFillOrders(buyOrder, sellOrder);
        }
    }

    private void partiallyFillOrders(Order buyOrder, Order sellOrder) {
    }

    private void fillOrders(Order buyOrder, Order sellOrder) {
        clientService.deposit(new ClientOperationDto(sellOrder.getClient(),
                sellOrder.getCurrencyPair().getTo(), buyOrder.getTotalPrice()));

        clientService.deposit(new ClientOperationDto(buyOrder.getClient(),
                buyOrder.getCurrencyPair().getTo(), sellOrder.getTotalPrice()));

        buyOrder.setStatus(OrderStatus.FILL);
        sellOrder.setStatus(OrderStatus.FILL);
    }

    @Override
    public void revoke(Order order) {
        clientService.deposit(new ClientOperationDto(order.getClient(),
                order.getCurrencyPair().getFrom(), order.getTotalPrice()));
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public List<Order> getOrders(Currency currency) {
        //TODO: implement method
        return null;
    }

    @Override
    public List<Order> getAllOrders() {
        //TODO: implement method
        return null;
    }

    private void checkMoneyToCreateOrder(OrderOperationDto orderOperationDto)
            throws NotEnoughMoneyException {
//        Currency currency = OrderDirection.BUY.equals(orderOperationDto.getOrderDirection()) ?
//                orderOperationDto.getCurrencyPair().getTo() : orderOperationDto.getCurrencyPair().getFrom();
        BigDecimal balance = orderOperationDto.getClient().getAccounts()
                .get(orderOperationDto.getCurrencyPair().getFrom());
        BigDecimal needMoney = orderOperationDto.getTotalPrice();
        if (needMoney.compareTo(balance) > 0) {
            throw new NotEnoughMoneyException(
                    String.format("Client id: %d, not enough %s for create order; required: %f; access: %f;",
                            orderOperationDto.getClient().getId(),
                            orderOperationDto.getCurrencyPair().getTo(),
                            needMoney,
                            balance));
        }
    }
}
