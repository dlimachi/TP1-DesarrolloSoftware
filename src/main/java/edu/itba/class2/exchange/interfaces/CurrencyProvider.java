package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.model.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CurrencyProvider {
    public Currency getCurrencyFromCode(String code);
    public Map<Currency, BigDecimal> getExchangeRates(Currency fromCurrency, List<Currency> toCurrencies);
}
