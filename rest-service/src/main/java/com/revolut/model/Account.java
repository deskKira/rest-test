package com.revolut.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

/**
 * Created by monster on 04.07.17.
 */

@Data
public class Account {

    Long accId;

    String accOwnerName;

    Long accountNumber;

    Long amount;

    String type;

    Long accUserId;

    public Account() {
    }

    public Account(Long accId, String accOwnerName, Long accountNumber, Long amount, String type, Long accUserId) {
        this.accId = accId;
        this.accOwnerName = accOwnerName;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.accUserId = accUserId;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accId=" + accId +
                ", accOwnerName='" + accOwnerName + '\'' +
                ", accountNumber=" + accountNumber +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
