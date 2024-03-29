package com.shishkin.service.implementation.simple;

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
    public void processOrder(Order order, List<Order> orders) {
        List<Order> matchOrders = orders.stream()
                .filter(item -> matchOrdersFilter(item, order))
                .sorted(Comparator.comparing(Order::getPrice))
                .toList();
        matchOrders.forEach(matchOrder -> this.execute(matchOrder, order));
        if (order.getAmount().compareTo(BigDecimalUtils.round(BigDecimal.ZERO)) > 0) {
            orders.add(order);
        }
    }

    protected boolean matchOrdersFilter(Order order, Order anotherOrder) {
        return filterByType(order, anotherOrder) && filterByPrice(order, anotherOrder) && filterByClient(order, anotherOrder)
                && OrderStatus.isActiveOrder(order) && OrderStatus.isActiveOrder(anotherOrder);
    }

    protected boolean filterByType(Order order, Order anotherOrder) {
        return !order.getOrderDirection().equals(anotherOrder.getOrderDirection());
    }

    protected boolean filterByClient(Order order, Order anotherOrder) {
        return !order.getClient().equals(anotherOrder.getClient());
    }

    protected boolean filterByPrice(Order order, Order anotherOrder) {
        if (OrderDirection.BUY.equals(order.getOrderDirection())) {
            return order.getPrice().compareTo(anotherOrder.getPrice()) >= 0;
        } else {
            return order.getPrice().compareTo(anotherOrder.getPrice()) <= 0;
        }
    }

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
        if (buyOrder.getAmount().compareTo(sellOrder.getAmount()) == 0) {
            fillOrders(buyOrder, sellOrder);
        } else {
            partiallyFillOrders(buyOrder, sellOrder);
        }
    }

    private void partiallyFillOrders(Order buyOrder, Order sellOrder) {
        BigDecimal amount = buyOrder.getAmount().min(sellOrder.getAmount());
        BigDecimal price = sellOrder.getPrice();

        partiallyFillOrder(buyOrder, amount, price);
        partiallyFillOrder(sellOrder, amount, price);

    }

    private void partiallyFillOrder(Order order, BigDecimal amount, BigDecimal price) {
        BigDecimal transferTotal = OrderDirection.BUY.equals(order.getOrderDirection())
                ? amount : BigDecimalUtils.round(amount.multiply(price));
        BigDecimal totalChange = OrderDirection.SELL.equals(order.getOrderDirection())
                ? amount : BigDecimalUtils.round(amount.multiply(price));

        order.setAmount(order.getAmount().subtract(amount));
        order.setTotalPrice(order.getTotalPrice().subtract(totalChange));

        depositByExecuteOrder(order, transferTotal);
        changeStatusByAmount(order);
        if (OrderStatus.FILL.equals(order.getStatus())) this.revoke(order);
    }

    private void fillOrders(Order buyOrder, Order sellOrder) {
        BigDecimal totalSell = BigDecimalUtils.round(sellOrder.getAmount().multiply(sellOrder.getPrice()));
        BigDecimal amountSell = sellOrder.getAmount();
        fillOrder(sellOrder, totalSell, BigDecimal.ZERO);
        fillOrder(buyOrder, amountSell, totalSell);
    }

    private void fillOrder(Order order, BigDecimal transfer, BigDecimal diffTotal) {
        depositByExecuteOrder(order, transfer);
        order.setTotalPrice(
                order.getOrderDirection().equals(OrderDirection.SELL) ? BigDecimalUtils.round(BigDecimal.ZERO) :
                order.getTotalPrice().subtract(diffTotal));
        order.setAmount(BigDecimalUtils.round(BigDecimal.ZERO));
        order.setStatus(OrderStatus.FILL);
        revoke(order);
    }

    @Override
    public void revoke(Order order) {
        if(order.getTotalPrice().compareTo(BigDecimalUtils.round(BigDecimal.ZERO)) > 0) {
            CLIENT_SERVICE.deposit(new ClientOperationDto(order.getClient(),
                    getCurrencyByOrder(order), order.getTotalPrice()));
            order.setTotalPrice(BigDecimalUtils.round(BigDecimal.ZERO));
            order.setStatus(OrderStatus.CANCELLED);
        }
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
                    orderOperationDto.getClient(), getCurrencyByOrder(orderOperationDto),
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

    private void depositByExecuteOrder(Order order, BigDecimal amount) {
        Currency currency = order.getOrderDirection().equals(OrderDirection.SELL) ?
                order.getCurrencyPair().getTo() : order.getCurrencyPair().getFrom();
        CLIENT_SERVICE.deposit(new ClientOperationDto(
                order.getClient(), currency, amount));
    }

    private void changeStatusByAmount(Order order) {
        if (BigDecimalUtils.round(BigDecimal.ZERO).compareTo(order.getAmount()) == 0) {
            order.setStatus(OrderStatus.FILL);
        } else {
            order.setStatus(OrderStatus.PARTIALLYFILL);
        }
    }

    private Currency getCurrencyByOrder(OrderOperationDto orderOperationDto) {
        return orderOperationDto.getOrderDirection().equals(OrderDirection.BUY) ?
        orderOperationDto.getCurrencyPair().getTo() : orderOperationDto.getCurrencyPair().getFrom();
    }

    private Currency getCurrencyByOrder(Order order) {
        return order.getOrderDirection().equals(OrderDirection.BUY) ?
                order.getCurrencyPair().getTo() : order.getCurrencyPair().getFrom();
    }
}
