package edu.itba.class2.exchange.provider;

import edu.itba.class2.exchange.config.ConfigurationManager;
import edu.itba.class2.exchange.exception.InvalidCurrencyException;
import edu.itba.class2.exchange.exception.InvalidDateException;
import edu.itba.class2.exchange.exception.ProviderException;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.HttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import edu.itba.class2.exchange.currency.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FreeCurrencyApiProviderTest {

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
    @DisplayName("Should fetch historical exchange rates and resolve corresponding currency metadata")
    void testGetHistoricalExchangeRates() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        var localDate = "2024-01-25";

        String historicalExchangeRates = """
                {"data":{"2024-01-25":{"CAD":1.46,"USD":1.08}}}
                """;
        String currencyUSD = """
                    {"data":{"USD":{"code":"USD","name":"US Dollar","symbol":"$"}}}
                """;
        String currencyCAD = """
                    {"data":{"CAD":{"code":"CAD","name":"Canadian Dollar","symbol":"CA$"}}}
                """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(200,historicalExchangeRates))
                .thenReturn(new HttpResponse(200,currencyCAD))
                .thenReturn(new HttpResponse(200,currencyUSD));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        final var historicalExchangeRatesResponse = provider.getHistoricalExchangeRates("EUR",List.of("CAD","USD"), LocalDate.parse(localDate));

        assertEquals(1,historicalExchangeRatesResponse.size());
        assertTrue(historicalExchangeRatesResponse.containsKey(localDate));

        Map<Currency,BigDecimal> ratesForDate = historicalExchangeRatesResponse.get(localDate);
        var ratesMap = ratesForDate.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().code(), Map.Entry::getValue));
        assertEquals(2,ratesForDate.size());
        assertEquals(ratesMap.get("CAD"),BigDecimal.valueOf(1.46));
        assertEquals(ratesMap.get("USD"),BigDecimal.valueOf(1.08));

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
                .thenReturn(new HttpResponse(200,historicalExchangeRates));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        assertThrows(
                ProviderException.class,
                () -> provider.getHistoricalExchangeRates(
                        "EUR",List.of("CAD","USD"),
                        LocalDate.parse("2024-01-25")
                )
        );
    }

    @Test
    @DisplayName("Should throw InvalidDateException due to missing date")
    void testGetHistoricalExchangeRatesWithMissingDate() {
        ConfigurationManager config = mock(ConfigurationManager.class);
        HttpClient httpClient = mock(HttpClient.class);

        /* Missing closing curly brace at the end */
        String historicalExchangeRates = """
            {
                "message": "Validation error",
                "errors": {
                    "date": [
                        "The date is not a valid date.",
                        "The date must be a date after or equal to 1999-01-01."
                    ]
                },
                "info": "For more information, see documentation: https://freecurrencyapi.com/docs/status-codes#_422"
            }
            """;

        when(httpClient.get(any(HttpGetRequest.class)))
                .thenReturn(new HttpResponse(422,historicalExchangeRates));

        final var provider = new FreeCurrencyApiProvider(httpClient, config);

        assertThrows(
                InvalidDateException.class,
                () -> provider.getHistoricalExchangeRates(
                        "EUR",List.of("CAD","USD"),
                        LocalDate.parse("2024-01-25")
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
                        "EUR",List.of("CAD","USD"),
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
                () -> provider.getExchangeRates("",List.of())
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
