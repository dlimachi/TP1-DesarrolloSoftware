package edu.itba.class2.exchange;

import edu.itba.class2.exchange.exception.ConversionServerUnavailable;
import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.interfaces.HttpClient;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

	@Test
	void testConvert() {
		// Given
		final var httpClient = mock(HttpClient.class);
		when(httpClient.get(anyString(), anyMap(), anyMap()))
				.thenReturn(new HttpResponse(200, "{\"data\":{\"USD\":1.05}}"));

		final var converter = new CurrencyConverter(httpClient);

		// When
		final var result = converter.convert("EUR", "USD", 100);

		// Then
		assertThat(result, closeTo(105, 0.01));
	}

	@Test
	void testConvertFailsWhenInternalServerError() {
		// Given
		final var httpClient = mock(HttpClient.class);
		when(httpClient.get(anyString(), anyMap(), anyMap()))
				.thenReturn(new HttpResponse(500, "Fatal error in server"));

		final var converter = new CurrencyConverter(httpClient);

		// When, Then
		assertThrows(ConversionServerUnavailable.class, () -> converter.convert("EUR", "USD", 100));
	}
}