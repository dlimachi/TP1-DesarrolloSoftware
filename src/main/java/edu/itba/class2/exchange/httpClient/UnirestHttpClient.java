package edu.itba.class2.exchange.httpClient;

import edu.itba.class2.exchange.interfaces.HttpClient;
import kong.unirest.core.Unirest;

public class UnirestHttpClient implements HttpClient {

	@Override
	public HttpResponse get(HttpGetRequest request) {
		try {
			final var response = Unirest
					.get(request.getUrl())
					.queryString(request.getParameters())
					.headers(request.getHeaders())
					.asString();
			return new HttpResponse(response.getStatus(), response.getBody());
		} catch (final Exception e) {
			System.err.println("Error: " + e.getMessage());
			return new HttpResponse(500, "Internal Server Error");
		}
	}


}
