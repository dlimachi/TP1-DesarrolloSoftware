package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.httpClient.HttpGetRequest;
import edu.itba.class2.exchange.httpClient.HttpResponse;

public interface HttpClient {
    HttpResponse get(HttpGetRequest request);
}
