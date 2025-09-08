package edu.itba.class2.exchange;

import edu.itba.class2.exchange.config.ConfigurationManager;
import edu.itba.class2.exchange.exception.InvalidCurrencyException;
import edu.itba.class2.exchange.exception.ProviderException;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.HttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FreeCurrencyApiProviderTest {
    @Test
    @DisplayName("Should fetch currency details (code, name, symbol) when a valid code is provided")
    void testGetCurrencyFromCode() {
        final var config = mock(ConfigurationManager.class);
        final var httpClient = mock(HttpClient.class);

        final var currencyUSD = """
                    {"data":{"USD":{"code":"USD","name":"US Dollar","symbol":"$"}}}
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(200, currencyUSD));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);
        final var currency = provider.getCurrencyFromCode("USD");
        assertEquals(currency.code(), "USD");
        assertEquals(currency.name(), "US Dollar");
        assertEquals(currency.symbol(), "$");
    }

    @Test
    @DisplayName("Should fetch latest exchange rates and resolve corresponding currency metadata")
    void testGetExchangeRates() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        String exchangeRatesJson = """
                    {"data":{"USD":1.05,"EUR":0.95}}
                """;
        String currencyUSD = """
                    {"data":{"USD":{"code":"USD","name":"US Dollar","symbol":"$"}}}
                """;
        String currencyEUR = """
                    {"data":{"EUR":{"code":"EUR","name":"EURO","symbol":"€"}}}
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(200, exchangeRatesJson)) // for latest
                .thenReturn(new HttpResponse(200, currencyUSD))   // for USD
                .thenReturn(new HttpResponse(200, currencyEUR));  // for EUR

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        final var rates = provider.getExchangeRates("ARS", List.of("USD", "EUR"));
        assertEquals(2, rates.size());
        var ratesMap = rates.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().code(), Map.Entry::getValue));
        assertEquals(ratesMap.get("USD"), BigDecimal.valueOf(1.05));
        assertEquals(ratesMap.get("EUR"), BigDecimal.valueOf(0.95));
    }

    @Test
    @DisplayName("Should throw InvalidCurrencyException when provider returns 422 with invalid currency error")
    void testInvalidCurrencyError() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        final var httpClient = mock(HttpClient.class);
        final var errorJson = """
                    {"errors":{"base_currency":["invalid currency"]}}
                """;
        when(httpClient.get(any(HttpGetRequest.class))).thenReturn(new HttpResponse(422, errorJson));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);
        assertThrows(InvalidCurrencyException.class, () -> provider.getCurrencyFromCode("EUR"));
    }

    @ParameterizedTest
    @MethodSource("errorCases")
    @DisplayName("Should map HTTP error responses to the corresponding ProviderException messages")
    void testProviderErrors(TestCase testCase) {
        ConfigurationManager config = mock(ConfigurationManager.class);
        final var httpClient = mock(HttpClient.class);
        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(testCase.status, ""));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        ProviderException ex = assertThrows(
                ProviderException.class,
                () -> provider.getCurrencyFromCode("")
        );
        assertEquals(testCase.expectedMessage, ex.getMessage());
    }

    private static Stream<TestCase> errorCases() {
        return Stream.of(
                new TestCase(401, "Invalid authentication credentials"),
                new TestCase(403, "Invalid endpoint"),
                new TestCase(404, "Invalid endpoint"),
                new TestCase(429, "Rate limit exceeded"),
                new TestCase(500, "Provider server error")
        );
    }

    private record TestCase(int status, String expectedMessage) {
    }
}
