package edu.itba.class2.boot;

import edu.itba.class2.exchange.currency.CurrencyConverter;
import edu.itba.class2.exchange.exception.InvalidCurrencyException;
import edu.itba.class2.exchange.exception.ProviderException;
import edu.itba.class2.exchange.httpClient.UnirestHttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;

import java.math.BigDecimal;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient);
		final var converter = new CurrencyConverter(provider);
		try {
			converter.convert("EUR", List.of("EUR", "USD", "JPY"), BigDecimal.valueOf(100.5));
		} catch (ProviderException pe) {

		} catch (InvalidCurrencyException ce) {

		}
		System.out.println(converter.convert("EUR", List.of("EUR", "USD", "JPY"), BigDecimal.valueOf(100.5)));
	}
}
