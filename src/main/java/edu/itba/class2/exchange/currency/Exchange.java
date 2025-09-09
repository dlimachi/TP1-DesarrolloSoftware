package edu.itba.class2.exchange.currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Exchange(Currency currency, BigDecimal amount, BigDecimal exchangeRate) {
}

