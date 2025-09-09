package edu.itba.class2.exchange.currency;

public record Currency(String code, String name, String symbol) {
    public String toString() {
        return String.format("%s (%s)", name, code);
    }
}
