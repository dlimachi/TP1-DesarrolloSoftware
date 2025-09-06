package edu.itba.class2.exchange.provider;

import com.google.gson.Gson;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import edu.itba.class2.exchange.interfaces.HttpClient;
import edu.itba.class2.exchange.currency.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeCurrencyApiProvider implements CurrencyProvider {
    private final HttpClient httpClient;
    private final String apiUrl = "https://api.freecurrencyapi.com/v1/latest";
    private final String apiKey = "fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6";

    FreeCurrencyApiProvider(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Currency getCurrencyFromCode(String code) {
        final var request = new HttpGetRequest.HttpGetRequestBuilder()
                .setUrl(apiUrl)
                .setHeader("Accept", "application/json")
                .setHeader("apiKey", apiKey)
                .setParameter("currencies", code)
                .build();
        final var response = httpClient.get(request);

        if (response.status() != 200) {
            System.err.println("Error: " + response.status());
            throw new RuntimeException("Error: " + response.status());
        }

        final var currencyRetrieved = new Gson().fromJson(response.body(), FreeCurrencyCurrenciesApiResponse.class);
        return currencyRetrieved.getData().get(code);
    }

    @Override
    public Map<Currency, BigDecimal> getExchangeRates(Currency fromCurrency, List<Currency> toCurrencies) {
        final var currencyList = toCurrencies.stream()
                .map(Currency::code)
                .collect(Collectors.joining(","));
        final var request = new HttpGetRequest.HttpGetRequestBuilder()
                .setUrl(apiUrl)
                .setHeader("Accept", "application/json")
                .setHeader("apiKey", apiKey)
                .setParameter("base_currency", fromCurrency)
                .setParameter("currencies", currencyList)
                .build();
        final var response = httpClient.get(request);

        // Check if the response is successful (status code 200).
        if (response.status() != 200) {
            System.err.println("Error: " + response.status());
            throw new RuntimeException("Error: " + response.status());
        }

        final var exchangeRateResponse = new Gson().fromJson(response.body(), FreeCurrencyExchangeApiResponse.class);
        return exchangeRateResponse.getData().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> getCurrencyFromCode(e.getKey()),
                        Map.Entry::getValue
                ));
    }

    public class FreeCurrencyCurrenciesApiResponse {
        private Map<String, Currency> data;

        public Map<String, Currency> getData() {
            return data;
        }
    }

    public class FreeCurrencyExchangeApiResponse {
        private Map<String, BigDecimal> data;

        public Map<String, BigDecimal> getData() {
            return data;
        }
    }

}
