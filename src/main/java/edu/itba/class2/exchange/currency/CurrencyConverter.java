package edu.itba.class2.exchange.currency;

import edu.itba.class2.exchange.interfaces.CurrencyProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrencyConverter {

    private final CurrencyProvider currencyProvider;

    public CurrencyConverter(final CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
    }

    public Map<Currency, BigDecimal> convert(String fromCurrency, List<String> toCurrencies, BigDecimal amount) {
        try {
            Map<Currency, BigDecimal> exchangeRates = currencyProvider.getExchangeRates(fromCurrency, toCurrencies);
            return exchangeRates.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().multiply(amount)
                    ));
        } catch (final RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Map<LocalDate, List<Exchange>> getHistorical(String fromCurrency, List<String> toCurrencies, BigDecimal amount, LocalDate date) {
        try {
            Map<String, Map<Currency, BigDecimal>> historicalExchangeRates = currencyProvider.getHistoricalExchangeRates(fromCurrency, toCurrencies, date);

            return historicalExchangeRates.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> LocalDate.parse(e.getKey()),
                            e -> e.getValue().entrySet().stream()
                                    .map(entry -> new Exchange(
                                            entry.getKey(),
                                            entry.getValue().multiply(amount),
                                            entry.getValue()
                                    ))
                                    .collect(Collectors.toList())
                    ));

        } catch (final RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
