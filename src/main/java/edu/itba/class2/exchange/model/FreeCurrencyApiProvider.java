package edu.itba.class2.exchange.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import edu.itba.class2.exchange.interfaces.HttpClient;

import java.lang.reflect.Type;
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
        }

        final var exchangeRateResponse = new Gson().fromJson(response.body(), ApiResponse.class);
        return exchangeRateResponse.getData();
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
        }

        Type type = new TypeToken<ApiResponse<BigDecimal>>() {}.getType();
        final ApiResponse<BigDecimal> exchangeRateResponse = new Gson().fromJson(response.body(), type);
//        var a = exchangeRateResponse.getData();
//        var b = a.entrySet().stream().map((entry) -> {
//            var currency = new Currency(entry.getKey());
//            return ???;
//        }).collect(Collectors.toList());
    }

    private static class ApiResponse<T> {
        private Map<String, T> data;

        public void setData(Map<String, T> data) {
            this.data = data;
        }

        public Map<String, T> getData() {
            return data;
        }
    }
}
