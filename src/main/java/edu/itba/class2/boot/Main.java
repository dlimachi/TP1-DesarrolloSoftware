package edu.itba.class2.boot;

import edu.itba.class2.exchange.config.DiskConfigurationManager;
import edu.itba.class2.exchange.currency.CurrencyConverter;
import edu.itba.class2.exchange.httpClient.UnirestHttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final var config = new DiskConfigurationManager("freecurrencyapi.properties");
        final var httpClient = new UnirestHttpClient();
        final var provider = new FreeCurrencyApiProvider(httpClient, config);
        final var converter = new CurrencyConverter(provider);

        var fromCurrencies = List.of("USD", "EUR");
        var toCurrencies = List.of("USD", "JPY", "GBP");
        var amounts = List.of(
                BigDecimal.valueOf(100.5),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(200)
        );
        var date = LocalDate.parse("2024-11-20");

        runConversions(converter, fromCurrencies, toCurrencies, amounts);
        runHistoricalConversions(converter, fromCurrencies, toCurrencies, amounts, date);
    }

    private static void runConversions(CurrencyConverter converter,
                                       List<String> fromCurrencies,
                                       List<String> toCurrencies,
                                       List<BigDecimal> amounts) {
        System.out.println("=== Converting currencies ===");
        fromCurrencies.forEach(from -> {
            sleep();
            System.out.println("\nFrom: " + from);
            amounts.forEach(amount -> {
                System.out.println("  Amount: " + amount);
                System.out.println("  Result: " + converter.convert(from, toCurrencies, amount));
            });
        });
    }

    private static void runHistoricalConversions(CurrencyConverter converter,
                                                 List<String> fromCurrencies,
                                                 List<String> toCurrencies,
                                                 List<BigDecimal> amounts,
                                                 LocalDate date) {
        System.out.println("\n=== Historical conversions (" + date + ") ===");
        String from = fromCurrencies.getFirst();
        System.out.println("\nFrom: " + from);
        amounts.forEach(amount -> {
            sleep();
            System.out.println("  Amount: " + amount);
            System.out.println("  Result: " + converter.getHistorical(from, toCurrencies, amount, date));
        });
    }

    private static void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
