package edu.itba.class2.exchange.currency;

import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("Should correctly convert amounts from USD to EUR and GBP using exchange rates")
    void testConvert_Successful() {
        Currency eur = new Currency("EUR", "Euro", "€");
        Currency gbp = new Currency("GBP", "British Pound", "£");

        when(currencyProvider.getExchangeRates("USD", List.of("EUR", "GBP"))).thenReturn(Map.of(
                eur, BigDecimal.valueOf(0.9),
                gbp, BigDecimal.valueOf(0.8)
        ));

        Map<Currency, BigDecimal> result = converter.convert("USD", List.of("EUR", "GBP"), BigDecimal.valueOf(100));

        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(90.0), result.get(eur));  // 100 * 0.9
        assertEquals(BigDecimal.valueOf(80.0), result.get(gbp));  // 100 * 0.8
    }
}