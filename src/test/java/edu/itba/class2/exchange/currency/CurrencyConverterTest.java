package edu.itba.class2.exchange.currency;

import edu.itba.class2.exchange.interfaces.CurrencyProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
    void testConvertSuccessful() {
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

    @Test
    @DisplayName("Should throw RuntimeException when getExchangeRates throws RuntimeException")
    void testConvertExceptionOnGetExchangeRates() {
        when(currencyProvider.getExchangeRates(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(
                RuntimeException.class,
                () -> converter.convert("USD", List.of("EUR", "GBP"), BigDecimal.valueOf(100))
        );
    }

    @Test
    @DisplayName("Should throw RuntimeException when getHistoricalExchangeRates throws RuntimeException")
    void testConvertExceptionOnGetHistoricalExchangeRates() {
        when(currencyProvider.getHistoricalExchangeRates(
                "USD", List.of("EUR", "GBP"), LocalDate.now())
        ).thenThrow(new RuntimeException());

        assertThrows(
                RuntimeException.class,
                () -> converter.getHistorical("USD", List.of("EUR", "GBP"), BigDecimal.valueOf(100),
                        LocalDate.now())
        );
    }

    @Test
    @DisplayName("Should correctly convert amounts from USD to EUR and GBP using exchange rates on a specific date ")
    void testHistoricalExchange(){
        var eur = new Currency("EUR", "Euro", "€");
        var gbp = new Currency("GBP", "British Pound", "£");

        var date = LocalDate.parse("2023-02-14");

        when(currencyProvider.getHistoricalExchangeRates("USD",List.of("EUR", "GBP"),date ))
                .thenReturn(Map.of("2023-02-14",Map.of(eur,BigDecimal.valueOf(0.93),gbp,BigDecimal.valueOf(0.82))));
        

        Map<LocalDate,List<Exchange>> result = converter.getHistorical("USD",List.of("EUR","GBP"),BigDecimal.valueOf(100),date);

        assertEquals(1,result.size());
        assertTrue(result.containsKey(date));

        var eurExchange = new Exchange(eur,BigDecimal.valueOf(93.00).setScale(2, RoundingMode.HALF_UP),BigDecimal.valueOf(0.93));
        var gbpExchange = new Exchange(gbp,BigDecimal.valueOf(82.00).setScale(2, RoundingMode.HALF_UP),BigDecimal.valueOf(0.82));

        var exchangesByDate = result.get(date);
        assertEquals(2,exchangesByDate.size());
        assertTrue(exchangesByDate.contains(eurExchange));
        assertTrue(exchangesByDate.contains(gbpExchange));

    }
}