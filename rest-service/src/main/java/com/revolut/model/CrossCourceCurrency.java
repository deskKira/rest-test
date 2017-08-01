package com.revolut.model;

public enum CrossCourceCurrency {

    EUROTOUSD(1.1664),
    USDTOEURO(0.8570),
    EUROTORUB(68.7980),
    RUBTOEURO(0.0144),
    USDTORUB(59.3829),
    RUBTOUSD(0.0168);

    private CrossCourceCurrency(double factor) {
        this.factor =factor;
    }

    public double getFactor() {
        return factor;
    }


    private double factor;

}
