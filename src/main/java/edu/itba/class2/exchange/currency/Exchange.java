package edu.itba.class2.exchange.currency;

import java.math.BigDecimal;

public record Exchange(Currency currency, BigDecimal amount, BigDecimal exchangeRate) {
}

