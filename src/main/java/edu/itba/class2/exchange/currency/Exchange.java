package edu.itba.class2.exchange.currency;

import java.math.BigDecimal;

public record Exchange(Currency currency, BigDecimal amount, BigDecimal exchangeRate) {
    @Override
    public String toString() {
        return String.format("%s = %s%.2f (rate: %.4f)",
                currency.code(),
                currency.symbol(),
                amount, exchangeRate);
    }
}

