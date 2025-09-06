package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.currency.Currency;
import java.util.Set;

public interface CurrencyInfo {
    public Set<Currency> getAvailableCurrencies();
}
