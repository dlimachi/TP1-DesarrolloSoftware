package edu.itba.class2.exchange.currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate date) {
}
