package edu.itba.class2.boot;

import edu.itba.class2.exchange.currency.CurrencyConverter;
import edu.itba.class2.exchange.httpClient.UnirestHttpClient;

public class Main {
	public static void main(String[] args) {
		final var httpClient = new UnirestHttpClient();
		final var converter = new CurrencyConverter(httpClient);
		System.out.println(converter.convert("EUR", "USD", 100));
	}
}
