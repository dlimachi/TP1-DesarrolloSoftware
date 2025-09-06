package edu.itba.class2.exchange;

import edu.itba.class2.exchange.currency.Currency;
import edu.itba.class2.exchange.currency.CurrencyConverter;
import edu.itba.class2.exchange.exception.ConversionServerUnavailable;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.HttpClient;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

    @Test
    void testConvert() {
        // Given
        final var provider = mock(FreeCurrencyApiProvider.class);
        final var currency = new Currency("EUR", "Euro", "€");
        when(provider.getExchangeRates(anyString(), anyList())).thenReturn(Map.of(currency, BigDecimal.TWO));

        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.convert("", List.of(), BigDecimal.TEN);

        // Then
        assertEquals(result, Map.of(currency, BigDecimal.valueOf(20)));
    }

//    @Test
//    void testConvertFailsWhenInternalServerError() {
//        // Given
//        final var httpClient = mock(HttpClient.class);
//        when(httpClient.get(anyString(), anyMap(), anyMap()))
//                .thenReturn(new HttpResponse(500, "Fatal error in server"));
//
//        final var converter = new CurrencyConverter(httpClient);
//
//        // When, Then
//        assertThrows(ConversionServerUnavailable.class, () -> converter.convert("EUR", "USD", 100));
//    }
}