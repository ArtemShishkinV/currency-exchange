package com.shishkin.service.implementation;

import com.shishkin.dto.ClientOperationDto;
import com.shishkin.dto.OrderOperationDto;
import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.Currency;
import com.shishkin.model.currency.CurrencyPair;
import com.shishkin.model.order.Order;
import com.shishkin.model.order.OrderDirection;
import com.shishkin.model.order.OrderStatus;
import com.shishkin.service.ClientService;
import com.shishkin.service.OrderService;
import com.shishkin.utils.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class OrderServiceImpl implements OrderService {
    private static final ClientService CLIENT_SERVICE = new ClientServiceImpl();

    @Override
    public Order createOrder(OrderOperationDto orderOperationDto) throws NotEnoughMoneyException {
        withdrawMoneyToCreateOrder(orderOperationDto);
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
        BigDecimal amount = buyOrder.getAmount().min(sellOrder.getAmount());
        BigDecimal price = getPriceByOrderDirection(buyOrder, sellOrder);

        partiallyFillOrder(buyOrder, amount, price);
        partiallyFillOrder(sellOrder, amount, price);

    }

    private void partiallyFillOrder(Order order, BigDecimal amount, BigDecimal price) {
        BigDecimal transferTotal = OrderDirection.BUY.equals(order.getOrderDirection())
                ? amount : amount.multiply(price);
        BigDecimal totalChange = OrderDirection.SELL.equals(order.getOrderDirection())
                ? amount : amount.multiply(price);
        //TODO: SAVE ORDER AND ROLLBACK IN FINALLY
        order.setAmount(order.getAmount().subtract(amount));
        order.setTotalPrice(order.getTotalPrice().subtract(totalChange));

        depositByExecuteOrder(order, transferTotal);
        changeStatusByAmount(order);
    }

    private void fillOrders(Order buyOrder, Order sellOrder) {
        fillOrder(sellOrder, buyOrder.getTotalPrice());
        fillOrder(buyOrder, sellOrder.getTotalPrice());
    }

    private void fillOrder(Order order, BigDecimal amount) {
        depositByExecuteOrder(order, amount);
        order.setStatus(OrderStatus.FILL);
    }

    @Override
    public void revoke(Order order) {
        CLIENT_SERVICE.deposit(new ClientOperationDto(order.getClient(),
                order.getCurrencyPair().getFrom(), order.getTotalPrice()));
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public List<Order> getActiveOrders(Map<CurrencyPair, List<Order>> orders) {
        return orders.values().stream().flatMap(Collection::stream)
                .filter(OrderStatus::isActiveOrder)
                .toList();
    }

    @Override
    public List<Order> getAllOrders(Map<CurrencyPair, List<Order>> orders) {
        return orders.entrySet()
                .stream()
                .flatMap(item -> item.getValue().stream())
                .sorted(Comparator.comparing(Order::getStatus))
                .toList();
    }

    private void withdrawMoneyToCreateOrder(OrderOperationDto orderOperationDto)
            throws NotEnoughMoneyException {
        try {
            CLIENT_SERVICE.withdraw(new ClientOperationDto(
                    orderOperationDto.getClient(), orderOperationDto.getCurrencyPair().getFrom(),
                    orderOperationDto.getTotalPrice()
            ));
        } catch (NotEnoughMoneyException e) {
            Currency currency = orderOperationDto.getCurrencyPair().getTo();
            throw new NotEnoughMoneyException(
                    String.format("Client id: %d, not enough %s for create order; required: %f; access: %f;",
                            orderOperationDto.getClient().getId(),
                            currency,
                            orderOperationDto.getTotalPrice(),
                            orderOperationDto.getClient().getAccounts().get(currency)), e.getCause());
        }
    }

    private BigDecimal getPriceByOrderDirection(Order order, Order anotherOrder) {
        if (OrderDirection.BUY.equals(order.getOrderDirection())) {
            return order.getPrice().min(anotherOrder.getPrice());
        }
        return order.getPrice().max(anotherOrder.getPrice());
    }

    private void depositByExecuteOrder(Order order, BigDecimal amount) {
        CLIENT_SERVICE.deposit(new ClientOperationDto(
                order.getClient(), order.getCurrencyPair().getTo(), amount));
    }

    private void changeStatusByAmount(Order order) {
        //TODO: may be throw exception and rollback if negative number in result amount
        if (BigDecimalUtils.round(BigDecimal.ZERO).compareTo(order.getAmount()) == 0) {
            order.setStatus(OrderStatus.FILL);
        } else {
            order.setStatus(OrderStatus.PARTIALLYFILL);
        }
    }
}
