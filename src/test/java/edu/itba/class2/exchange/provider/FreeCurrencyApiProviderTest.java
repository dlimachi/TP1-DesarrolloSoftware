package edu.itba.class2.exchange.provider;

import com.google.gson.Gson;
import edu.itba.class2.exchange.config.ConfigurationManager;
import edu.itba.class2.exchange.currency.Currency;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.HttpClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FreeCurrencyApiProviderTest {

    private HttpClient httpClient;
    private FreeCurrencyApiProvider provider;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        ConfigurationManager config = mock(ConfigurationManager.class);

        when(config.getProperty("api.url")).thenReturn("https://fake.api");
        when(config.getProperty("api.key")).thenReturn("fake-key");

        provider = new FreeCurrencyApiProvider(httpClient, config);
    }

    @Test
    @DisplayName("Should fetch exchange rates and return mapped currencies with correct values")
    void testGetExchangeRates_Successful() {
        FreeCurrencyApiProvider.FreeCurrencyExchangeApiResponse exchangeResponse =
                new FreeCurrencyApiProvider.FreeCurrencyExchangeApiResponse(Map.of("USD", BigDecimal.valueOf(1.0), "EUR", BigDecimal.valueOf(0.9)));

        FreeCurrencyApiProvider.FreeCurrencyCurrenciesApiResponse currencyResponse =
                new FreeCurrencyApiProvider.FreeCurrencyCurrenciesApiResponse(Map.of(
                        "USD", new Currency("USD", "US Dollar", "$"),
                        "EUR", new Currency("EUR", "Euro", "€")
                ));

        var exchangeHttpResponse = new HttpResponse(200, new Gson().toJson(exchangeResponse));
        var currencyHttpResponse = new HttpResponse(200, new Gson().toJson(currencyResponse));

        when(httpClient.get(ArgumentMatchers.any(HttpGetRequest.class)))
                .thenReturn(exchangeHttpResponse)
                .thenReturn(currencyHttpResponse);

        Map<Currency, BigDecimal> result = provider.getExchangeRates("USD", List.of("EUR"));

        assertEquals(2, result.size());
        Currency eur = new Currency("EUR", "Euro", "€");
        assertTrue(result.containsKey(eur));
        assertEquals(BigDecimal.valueOf(0.9), result.get(eur));
    }
}

