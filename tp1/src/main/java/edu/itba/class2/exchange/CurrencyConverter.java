package edu.itba.class2.exchange;

import com.google.gson.Gson;

import java.util.Map;

public class CurrencyConverter {

	private final HttpClient httpClient;

	public CurrencyConverter(final HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public double convert(String fromCurrency, String toCurrency, double amount) {
		try {
			// Query the API using API Key, base currency and target currency.
			final var response = this.httpClient.get("https://api.freecurrencyapi.com/v1/latest",
					Map.of("base_currency", fromCurrency, "currencies", toCurrency), Map.of("accept",
							"application/json", "apikey", "fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6"));

			// Check if the response is successful (status code 200).
			if (response.status() != 200) {
				System.err.println("Error: " + response.status());
			}

			// Parse the response body to a Java object.
			final var exchangeRateResponse = new Gson().fromJson(response.body(), ExchangeRateResponse.class);

			// Calculate the exchange rate and return the result.
			return amount * exchangeRateResponse.getExchange(toCurrency);
		} catch (final Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}

	// Define a nested class to represent the response body.
	private static class ExchangeRateResponse {
		private Map<String, Double> data;

		public void setData(Map<String, Double> data) {
			this.data = data;
		}

		public double getExchange(final String toCurrency) {
			return this.data.get(toCurrency);
		}
	}

}
