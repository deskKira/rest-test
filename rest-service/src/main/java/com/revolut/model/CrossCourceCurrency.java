package com.revolut.model;

public enum CrossCourceCurrency {

    EURUSD(1.1664),
    USDEUR(0.8570),
    EURRUB(68.7980),
    RUBEUR(0.0144),
    USDRUB(59.3829),
    RUBUSD(0.0168);

    private CrossCourceCurrency(double factor) {
        this.factor =factor;
    }

    public double getFactor() {
        return factor;
    }


    private double factor;

}
