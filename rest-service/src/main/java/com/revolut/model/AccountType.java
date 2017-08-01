package com.revolut.model;

/**
 * Created by monster on 12.07.17.
 */
public enum AccountType {
    EURO(978), DOLLAR(840), RUBLE(643);

    private AccountType(int currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getCurrencyCode() {
        return currencyCode;
    }

    private int currencyCode;
}
