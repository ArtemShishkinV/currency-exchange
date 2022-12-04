package com.shishkin.model;

import com.shishkin.exception.NotEnoughMoneyException;
import com.shishkin.model.currency.Currency;
import com.shishkin.utils.BigDecimalUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public final class Client {
    private final Long id;
    private final Map<Currency, BigDecimal> accounts = new ConcurrentHashMap<>(Currency.values().length);

    private final Map<Currency, Object> locks = new ConcurrentHashMap<>(Currency.values().length);

    public Client() {
        this.id = UUID.randomUUID().getMostSignificantBits();
        fillAccounts();
    }

    private void fillAccounts() {
        for (Currency currency :
                Currency.values()) {
            this.accounts.putIfAbsent(currency, BigDecimalUtils.round(new BigDecimal(0)));
            this.locks.putIfAbsent(currency, new Object());
        }
    }

    public void deposit(Currency currency, BigDecimal amount) throws IllegalArgumentException {
        synchronized (this.locks.get(currency)) {
            if (amount.compareTo(BigDecimalUtils.round(BigDecimal.ZERO)) < 0) {
                throw new IllegalArgumentException("deposit amount must be more than zero!");
            }
            accounts.merge(currency, amount, BigDecimal::add);
        }
    }

    public void withdraw(Currency currency, BigDecimal amount) throws NotEnoughMoneyException {
        synchronized (this.locks.get(currency)) {
            if (amount.compareTo(this.accounts.get(currency)) > 0) {
                throw new NotEnoughMoneyException(
                        String.format("Client id: %d, not enough %s for withdraw; required: %f; access: %f;",
                                this.id,
                                currency,
                                amount,
                                this.accounts.get(currency)));
            }
            this.accounts.merge(currency, amount, BigDecimal::subtract);
        }
    }

    public Map<Currency, BigDecimal> getAccounts() {
        return new EnumMap<>(this.accounts);
    }
}
