package com.shishkin.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BigDecimalUtils {
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final int SCALE = 4;

    public static BigDecimal round(BigDecimal number) {
        return number.setScale(SCALE, ROUNDING_MODE);
    }
}
