package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.httpClient.HttpResponse;
import edu.itba.class2.exchange.httpClient.HttpGetRequest;

public interface HttpClient {
	HttpResponse get(HttpGetRequest request);
}
