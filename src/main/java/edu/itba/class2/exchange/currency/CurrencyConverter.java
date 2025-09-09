package edu.itba.class2.exchange.currency;

import edu.itba.class2.exchange.interfaces.CurrencyProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CurrencyConverter {

    private final CurrencyProvider currencyProvider;

    public CurrencyConverter(final CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
    }

    public List<ExchangeRateWithAmount> convert(String fromCurrency, List<String> toCurrencies, BigDecimal amount) {
        try {
            var exchangeRates = currencyProvider.getExchangeRates(fromCurrency, toCurrencies);
            return exchangeRates.stream().map(exchangeRate -> new ExchangeRateWithAmount(
                            amount.multiply(exchangeRate.rate()), exchangeRate
                    )
            ).toList();
        } catch (final RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<ExchangeRateWithAmount> getHistorical(String fromCurrency, List<String> toCurrencies, BigDecimal amount, LocalDate date) {
        try {
            var historicalExchangeRates = currencyProvider.getHistoricalExchangeRates(fromCurrency, toCurrencies, date);
            return historicalExchangeRates.stream().map(exchangeRate -> new ExchangeRateWithAmount(
                            amount.multiply(exchangeRate.rate()), exchangeRate
                    )
            ).toList();
        } catch (final RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
