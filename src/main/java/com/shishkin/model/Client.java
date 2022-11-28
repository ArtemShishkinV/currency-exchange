package com.shishkin.model;

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

    public Client() {
        this.id = UUID.randomUUID().getMostSignificantBits();
        fillAccounts();
    }

    private void fillAccounts() {
        for (Currency currency :
                Currency.values()) {
            this.accounts.putIfAbsent(currency, BigDecimalUtils.round(new BigDecimal(0)));
        }
    }

    public Map<Currency, BigDecimal> getAccounts() {
        return new EnumMap<>(this.accounts);
    }
}
