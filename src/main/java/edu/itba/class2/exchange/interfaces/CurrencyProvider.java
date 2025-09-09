package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CurrencyProvider {
    Currency getCurrencyFromCode(String code);
    Map<Currency, BigDecimal> getExchangeRates(String fromCurrency, List<String> toCurrencies);
    Map<String, Map<String,BigDecimal>> getHistoricalExchangeRates(String fromCurrency, List<String> toCurrencies, LocalDate date);
    }
