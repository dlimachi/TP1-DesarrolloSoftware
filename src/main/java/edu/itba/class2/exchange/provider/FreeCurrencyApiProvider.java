package edu.itba.class2.exchange.provider;

import com.google.gson.Gson;
import edu.itba.class2.exchange.config.ConfigurationManager;
import edu.itba.class2.exchange.currency.Currency;
import edu.itba.class2.exchange.currency.Exchange;
import edu.itba.class2.exchange.exception.InvalidCurrencyException;
import edu.itba.class2.exchange.exception.InvalidDateException;
import edu.itba.class2.exchange.exception.ProviderException;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import edu.itba.class2.exchange.interfaces.HttpClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeCurrencyApiProvider implements CurrencyProvider {
    private static final Gson GSON = new Gson();
    private final HttpClient httpClient;
    private final String apiUrl;
    private final String apiKey;

    public FreeCurrencyApiProvider(HttpClient httpClient, ConfigurationManager configurationManager) {
        this.httpClient = httpClient;
        this.apiUrl = configurationManager.getProperty("api.url");
        this.apiKey = configurationManager.getProperty("api.key");
    }

    private HttpGetRequest.HttpGetRequestBuilder basicRequestBuilder(String endpoint) {
        return new HttpGetRequest.HttpGetRequestBuilder()
                .setUrl(apiUrl + "/" + endpoint)
                .setHeader("Accept", "application/json")
                .setHeader("apiKey", apiKey);
    }

    private <T> T parseJson(HttpResponse response, Class<T> clazz) {
        try {
            return GSON.fromJson(response.body(), clazz);
        } catch (RuntimeException e) {
            throw new ProviderException("GSON parsing error: " + e.getMessage());
        }
    }

    private void handleUnprocessableEntity(HttpResponse response) {
        final var errors = parseJson(response, ErrorResponse.class).errors();
        if (errors.containsKey("base_currency") || errors.containsKey("currencies")) {
            throw new InvalidCurrencyException();
        } else if (errors.containsKey("date")) {
            throw new InvalidDateException();
        } else {
            throw new ProviderException("422 Unprocessable entity: " + response.body());
        }
    }

    private void handleErrorResponse(HttpResponse response) {
        switch (response.status()) {
            case 200 -> {}
            case 401 -> throw new ProviderException("Invalid authentication credentials");
            case 403, 404 -> throw new ProviderException("Invalid endpoint");
            case 429 -> throw new ProviderException("Rate limit exceeded");
            case 500 -> throw new ProviderException("Provider server error");
            case 422 -> handleUnprocessableEntity(response);
            default -> throw new ProviderException("Unexpected status code" + response.status());
        }
    }

    @Override
    public Currency getCurrencyFromCode(String code) {
        final var request = basicRequestBuilder("currencies")
                .setParameter("currencies", code)
                .build();
        final var response = httpClient.get(request);

        handleErrorResponse(response);

        final var currencyRetrieved = parseJson(response, FreeCurrencyCurrenciesApiResponse.class);
        return currencyRetrieved.data().get(code);
    }

    @Override
    public Map<Currency, BigDecimal> getExchangeRates(String fromCurrency, List<String> toCurrencies) {
        final var currencyList = String.join(",", toCurrencies);
        final var request = basicRequestBuilder("latest")
                .setParameter("base_currency", fromCurrency)
                .setParameter("currencies", currencyList)
                .build();
        final var response = httpClient.get(request);

        handleErrorResponse(response);

        final var exchangeRate = parseJson(response, FreeCurrencyExchangeApiResponse.class).data();
        return exchangeRate.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> getCurrencyFromCode(e.getKey()),
                        Map.Entry::getValue
                ));
    }

    @Override
    public Map<String, BigDecimal> getHistoricalExchangeRates(String fromCurrency, List<String> toCurrencies, LocalDate date){
        final var currencyList = String.join(",", toCurrencies);
        final var request = basicRequestBuilder("historical")
                .setParameter("base_currency", fromCurrency)
                .setParameter("currencies", currencyList)
                .setParameter("date",date.toString())
                .build();
        final var response = httpClient.get(request);

        handleErrorResponse(response);

        final var historicalExchangeRate = parseJson(response,FreeCurrencyHistoricalExchangeApiResponse.class).data();

        return historicalExchangeRate.get(date.toString());
    }

    public record FreeCurrencyCurrenciesApiResponse(Map<String, Currency> data) {

    }

    public record FreeCurrencyExchangeApiResponse(Map<String, BigDecimal> data) {
    }

    public record FreeCurrencyHistoricalExchangeApiResponse(Map<String,Map<String,BigDecimal>> data){
    }

    public record ErrorResponse(Map<String, List<String>> errors) {
    }
}
