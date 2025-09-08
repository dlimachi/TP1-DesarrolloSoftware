package edu.itba.class2.exchange.currency;

import java.math.BigDecimal;

public record Exchange(BigDecimal amount, BigDecimal exchangeRate) {
}

