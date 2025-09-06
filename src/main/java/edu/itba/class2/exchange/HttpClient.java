package edu.itba.class2.exchange;

import java.util.Map;

public interface HttpClient {

	HttpResponse get(String url, Map<String, Object> queryParams, Map<String, String> headers);
}
