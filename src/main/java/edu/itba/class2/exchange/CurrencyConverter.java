package edu.itba.class2.exchange;

import edu.itba.class2.exchange.interfaces.CurrencyProvider;

public class CurrencyConverter {

	private final CurrencyProvider currencyProvider;

	public CurrencyConverter(final CurrencyProvider currencyProvider) {
		this.currencyProvider = currencyProvider;
	}

	public double convert(String fromCurrency, String toCurrency, double amount) {
		try {
			// Query the API using API Key, base currency and target currency.
			var exchangeRates = currencyProvider.getExchangeRates();
			// Calculate the exchange rate and return the result.
			return amount * exchangeRateResponse.getExchange(toCurrency);
		} catch (final Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}
}
