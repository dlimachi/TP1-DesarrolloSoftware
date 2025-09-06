package edu.itba.class2.exchange;

public record HttpResponse(int status, String body) {

	@Override
	public String toString() {
		return "HttpResponse{" + "status=" + this.status + ", body='" + this.body + '\'' + '}';
	}
}
