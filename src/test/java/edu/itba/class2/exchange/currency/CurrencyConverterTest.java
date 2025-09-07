package edu.itba.class2.exchange.currency;

import edu.itba.class2.exchange.currency.CurrencyConverter;
import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import edu.itba.class2.exchange.provider.FreeCurrencyApiProvider;
import org.junit.jupiter.api.BeforeEach;
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

    private CurrencyProvider currencyProvider;
    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        currencyProvider = mock(CurrencyProvider.class);
        converter = new CurrencyConverter(currencyProvider);
    }

    @Test
    void testConvert_Successful() {
        // 1. Arrange
        Currency eur = new Currency("EUR", "Euro", "€");
        Currency gbp = new Currency("GBP", "British Pound", "£");

        when(currencyProvider.getExchangeRates("USD", List.of("EUR", "GBP"))).thenReturn(Map.of(
                eur, BigDecimal.valueOf(0.9),
                gbp, BigDecimal.valueOf(0.8)
        ));

        // 2. Act
        Map<Currency, BigDecimal> result = converter.convert("USD", List.of("EUR", "GBP"), BigDecimal.valueOf(100));

        // 3. Assert
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(90.0), result.get(eur));  // 100 * 0.9
        assertEquals(BigDecimal.valueOf(80.0), result.get(gbp));  // 100 * 0.8
    }
}