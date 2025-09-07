package edu.itba.class2.exchange;

import edu.itba.class2.exchange.config.ConfigurationManager;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.HttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FreeCurrencyApiProviderTest {
    @Test
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

        // TODO: check what this does.
        // verify interaction
//        verify(httpClient, atLeastOnce()).get(any(HttpGetRequest.class));
    }
}
