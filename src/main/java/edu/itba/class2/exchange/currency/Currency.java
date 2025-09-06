package edu.itba.class2.exchange.currency;

import java.util.Objects;

public record Currency(String code, String name, String symbol) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency currency)) return false;
        return Objects.equals(code, currency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
