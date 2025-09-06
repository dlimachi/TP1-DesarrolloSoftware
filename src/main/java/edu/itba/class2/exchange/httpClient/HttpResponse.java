package edu.itba.class2.exchange.httpClient;

public record HttpResponse(int status, String body) {

	@Override
	public String toString() {
		return "HttpResponse{" + "status=" + this.status + ", body='" + this.body + '\'' + '}';
	}
}
