package edu.itba.class2.exchange.currency;

import java.math.BigDecimal;

public record ExchangeRateWithAmount(BigDecimal amount, ExchangeRate exchangeRate) {
}
