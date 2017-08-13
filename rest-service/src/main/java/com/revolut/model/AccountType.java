package com.revolut.model;

/**
 * Created by monster on 12.07.17.
 */
public enum AccountType {
    EUR(978), USD(840), RUB(643);

    private AccountType(int currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getCurrencyCode() {
        return currencyCode;
    }

    private int currencyCode;
}
