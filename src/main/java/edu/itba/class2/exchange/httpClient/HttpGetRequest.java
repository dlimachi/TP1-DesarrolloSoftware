package edu.itba.class2.exchange.httpClient;

import java.util.HashMap;
import java.util.Map;

public class HttpGetRequest {
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, Object> parameters;

    private HttpGetRequest(String url, Map<String, String> headers, Map<String, Object> parameters) {
        this.url = url;
        this.headers = headers;
        this.parameters = parameters;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static class HttpGetRequestBuilder {
        private String url;
        private final Map<String, String> headers;
        private final Map<String, Object> parameters;

        public HttpGetRequestBuilder() {
            this.headers = new HashMap<>();
            this.parameters = new HashMap<>();
        }

        public HttpGetRequestBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public HttpGetRequestBuilder setHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public HttpGetRequestBuilder setParameter(String name, Object value) {
            parameters.put(name, value);
            return this;
        }

        public HttpGetRequest build() {
            return new HttpGetRequest(url, headers, parameters);
        }
    }
}
