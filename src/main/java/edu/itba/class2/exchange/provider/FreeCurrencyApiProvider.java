package edu.itba.class2.exchange.provider;

import com.google.gson.Gson;
import edu.itba.class2.exchange.currency.Currency;
import edu.itba.class2.exchange.exception.InvalidCurrencyException;
import edu.itba.class2.exchange.exception.InvalidDateException;
import edu.itba.class2.exchange.exception.ProviderException;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import edu.itba.class2.exchange.interfaces.HttpClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeCurrencyApiProvider implements CurrencyProvider {
    private static final Gson GSON = new Gson();
    private final HttpClient httpClient;
    private final String apiUrl = "https://api.freecurrencyapi.com/v1";
    private final String apiKey = "fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6";

    public FreeCurrencyApiProvider(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private HttpGetRequest.HttpGetRequestBuilder basicRequestBuilder(String endpoint) {
        return new HttpGetRequest.HttpGetRequestBuilder()
                .setUrl(apiUrl + "/" + endpoint)
                .setHeader("Accept", "application/json")
                .setHeader("apiKey", apiKey);
    }

    private void throwFromResponse(HttpResponse response) {
        switch (response.status()) {
            case 200 -> { return; }
            case 401 -> throw new ProviderException("Invalid authentication credentials");
            case 403, 404 -> throw new ProviderException("Invalid endpoint");
            case 429 -> throw new ProviderException("Rate limit exceeded");
            case 500 -> throw new ProviderException("Provider server error");
            case 422 -> {
                try {
                    final var errors = GSON.fromJson(response.body(), ErrorResponse.class).getErrors();
                    if (errors.containsKey("base_currency") || errors.containsKey("currencies")) {
                        throw new InvalidCurrencyException();
                    } else if (errors.containsKey("date")) {
                        throw new InvalidDateException();
                    } else {
                        throw new ProviderException("422 Unprocessable entity: " + response.body());
                    }
                } catch (Exception e) {
                    throw new ProviderException("Failed to parse 422 error response");
                }
            }
            default -> throw new ProviderException("Unexpected status code" + response.status());
        }
    }

    @Override
    public Currency getCurrencyFromCode(String code) {
        final var request = basicRequestBuilder("currencies")
                .setParameter("currencies", code)
                .build();
        final var response = httpClient.get(request);

        throwFromResponse(response);

        final var currencyRetrieved = GSON.fromJson(response.body(), CurrencyResponse.class);
        return currencyRetrieved.getData().get(code);
    }

    @Override
    public Map<Currency, BigDecimal> getExchangeRates(String fromCurrency, List<String> toCurrencies) {
        final var currencyList = String.join(",", toCurrencies);
        final var request = basicRequestBuilder("latest")
                .setParameter("base_currency", fromCurrency)
                .setParameter("currencies", currencyList)
                .build();
        final var response = httpClient.get(request);

        throwFromResponse(response);

        final var exchangeRateResponse = GSON.fromJson(response.body(), ExchangeResponse.class);
        return exchangeRateResponse.getData().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> getCurrencyFromCode(e.getKey()),
                        Map.Entry::getValue
                ));
    }

    public static class CurrencyResponse {
        private Map<String, Currency> data;

        public Map<String, Currency> getData() {
            return data;
        }
    }

    public static class ExchangeResponse {
        private Map<String, BigDecimal> data;

        public Map<String, BigDecimal> getData() {
            return data;
        }
    }

    public static class ErrorResponse {
        private Map<String, List<String>> errors;

        public Map<String, List<String>> getErrors() {
            return errors;
        }
    }
}
