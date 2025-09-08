package edu.itba.class2.boot;

import edu.itba.class2.exchange.config.DiskConfigurationManager;
import edu.itba.class2.exchange.currency.CurrencyConverter;
import edu.itba.class2.exchange.httpClient.UnirestHttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;

import java.math.BigDecimal;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		final var config = new DiskConfigurationManager("application.properties");
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, config);
		final var converter = new CurrencyConverter(provider);
		System.out.println(converter.convert("EUR", List.of("EUR", "USD", "JPY"), BigDecimal.valueOf(100.5)));
	}
}
