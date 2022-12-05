package com.shishkin.model.currency;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class CurrencyPair {
    Currency from;
    Currency to;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyPair that)) return false;
        return from == that.from && to == that.to || from == that.to && to == that.from;
    }

    @Override
    public int hashCode() {
        return from.hashCode() + to.hashCode();
    }
}
