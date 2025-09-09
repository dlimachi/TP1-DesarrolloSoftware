package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.currency.ExchangeRate;

import java.time.LocalDate;
import java.util.List;

public interface CurrencyProvider {
    List<ExchangeRate> getExchangeRates(String fromCurrency, List<String> toCurrencies);

    List<ExchangeRate> getHistoricalExchangeRates(String fromCurrency, List<String> toCurrencies, LocalDate date);
}
