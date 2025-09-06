package edu.itba.class2.exchange.interfaces;

import edu.itba.class2.exchange.model.Currency;
import java.util.Set;

public interface CurrencyInfo {
    public Set<Currency> getAvailableCurrencies();
}
