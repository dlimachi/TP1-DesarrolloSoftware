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
		System.out.println(converter.convert("EUR", List.of("EUR", "USD", "JPY"), BigDecimal.valueOf(100.5)));
		System.out.println(converter.getHistorical("EUR", List.of("EUR", "USD", "JPY"), BigDecimal.valueOf(100.5), LocalDate.parse("2024-11-20")));
	}
}
