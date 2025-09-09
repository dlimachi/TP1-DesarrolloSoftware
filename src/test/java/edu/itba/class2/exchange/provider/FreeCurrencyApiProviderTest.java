package edu.itba.class2.exchange.provider;

import edu.itba.class2.exchange.currency.Currency;
import edu.itba.class2.exchange.exception.InvalidCurrencyException;
import edu.itba.class2.exchange.exception.InvalidDateException;
import edu.itba.class2.exchange.exception.ProviderException;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.ConfigurationManager;
import edu.itba.class2.exchange.interfaces.HttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FreeCurrencyApiProviderTest {

    @Test
    @DisplayName("Should fetch latest exchange rates and resolve corresponding currency metadata")
    void testGetExchangeRates() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        String exchangeRatesJson = """
                    {"data":{"USD":1.05,"EUR":0.95}}
                """;
        String currenciesJson = """
                    {
                        "data": {
                            "USD":{"code":"USD","name":"US Dollar","symbol":"$"},
                            "EUR":{"code":"EUR","name":"EURO","symbol":"€"}
                        }
                    }
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(200, exchangeRatesJson)) // for latest
                .thenReturn(new HttpResponse(200, currenciesJson)); // for currencies

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        final var rates = provider.getExchangeRates("ARS", List.of("USD", "EUR"));
        assertEquals(2, rates.size());
        assertEquals(rates.get(0).rate(), BigDecimal.valueOf(1.05));
        assertEquals(rates.get(1).rate(), BigDecimal.valueOf(0.95));
    }

    @Test
    @DisplayName("Should fetch historical exchange rates and resolve corresponding currency metadata")
    void testGetHistoricalExchangeRates() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        var localDate = "2024-01-25";

        String historicalExchangeRates = """
                {"data":{"2024-01-25":{"CAD":1.46,"USD":1.08}}}
                """;
        String currenciesJson = """
                    {
                        "data": {
                            "USD":{"code":"USD","name":"US Dollar","symbol":"$"},
                            "CAD":{"code":"CAD","name":"Canadian Dollar","symbol":"CA$"}
                        }
                    }
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(200, historicalExchangeRates))
                .thenReturn(new HttpResponse(200, currenciesJson));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        final var historicalRates = provider.getHistoricalExchangeRates("EUR", List.of("CAD", "USD"), LocalDate.parse(localDate));

        assertEquals(2, historicalRates.size());
        assertEquals(historicalRates.get(0).rate(), BigDecimal.valueOf(1.46));
        assertEquals(historicalRates.get(1).rate(), BigDecimal.valueOf(1.08));

    }

    @Test
    @DisplayName("Should throw ProviderException due to GSON parse error")
    void testGetHistoricalExchangeRatesWithInvalidJson() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        /* Missing closing curly brace at the end */
        String historicalExchangeRates = """
                {"data":{"2024-01-25":{"CAD":1.46,"USD":1.08}}
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(200, historicalExchangeRates));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        assertThrows(
                ProviderException.class,
                () -> provider.getHistoricalExchangeRates(
                        "EUR", List.of("CAD", "USD"),
                        LocalDate.parse("2024-01-25")
                )
        );
    }

    @Test
    @DisplayName("Should throw InvalidDateException due to invalid date")
    void testGetHistoricalExchangeRatesWithMissingDate() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        String historicalExchangeRates = """
                {
                    "message": "Validation error",
                    "errors": {
                        "date": [
                            "The date must be a date after or equal to 1999-01-01."
                        ]
                    },
                    "info": "For more information, see documentation: https://freecurrencyapi.com/docs/status-codes#_422"
                }
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(422, historicalExchangeRates));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        assertThrows(
                InvalidDateException.class,
                () -> provider.getHistoricalExchangeRates(
                        "EUR", List.of("CAD", "USD"),
                        LocalDate.parse("1998-01-25")
                )
        );
    }

    @Test
    @DisplayName("Should throw ProviderException when 'errors' does not contain 'base_currency', 'currencies' or 'date'")
    void testUnprocessableEntityDefaultCase() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        final var httpClient = mock(HttpClient.class);
        final var errorJson = """
                    {"errors":{"this_is_an_invalid_error_key":null}}
                """;
        when(httpClient.get(any(HttpGetRequest.class))).thenReturn(new HttpResponse(422, errorJson));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);
        assertThrows(
                ProviderException.class,
                () -> provider.getHistoricalExchangeRates(
                        "EUR", List.of("CAD", "USD"),
                        LocalDate.parse("2024-01-25")
                )
        );
    }

    @Test
    @DisplayName("Should throw InvalidCurrencyException when provider returns 422 with invalid base_currency error")
    void testInvalidBaseCurrencyError() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        final var httpClient = mock(HttpClient.class);
        final var errorJson = """
                    {"errors":{"base_currency":["invalid currency"]}}
                """;
        when(httpClient.get(any(HttpGetRequest.class))).thenReturn(new HttpResponse(422, errorJson));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);
        assertThrows(InvalidCurrencyException.class, () -> provider.getExchangeRates("invalid", List.of("USD")));
    }

    @Test
    @DisplayName("Should throw InvalidCurrencyException when provider returns 422 with invalid currencies")
    void testInvalidCurrenciesError() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        final var httpClient = mock(HttpClient.class);
        final var errorJson = """
                    {"errors":{"currencies":["invalid currency"]}}
                """;
        when(httpClient.get(any(HttpGetRequest.class))).thenReturn(new HttpResponse(422, errorJson));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);
        assertThrows(InvalidCurrencyException.class, () -> provider.getExchangeRates("invalid", List.of("USD")));
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
                () -> provider.getExchangeRates("", List.of())
        );
        assertEquals(testCase.expectedMessage, ex.getMessage());
    }

    private static Stream<TestCase> errorCases() {
        return Stream.of(
                new TestCase(401, "Invalid authentication credentials"),
                new TestCase(403, "Invalid endpoint"),
                new TestCase(404, "Invalid endpoint"),
                new TestCase(429, "Rate limit exceeded"),
                new TestCase(500, "Provider server error"),
                new TestCase(123, "Unexpected status code" + 123)
        );
    }

    private record TestCase(int status, String expectedMessage) {
    }
}
