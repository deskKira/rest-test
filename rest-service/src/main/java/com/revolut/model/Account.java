package com.revolut.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

/**
 * Created by monster on 04.07.17.
 */

@Data
@DatabaseTable(tableName = "account")
public class Account {

    @DatabaseField(generatedId = true, columnName = "acc_id")
    Long accId;
    @DatabaseField(columnName = "acc_owner_name")
    String accOwnerName;
    @DatabaseField(columnName = "account_number")
    Long accountNumber;
    @DatabaseField(columnName = "amount")
    Long amount;

    @DatabaseField(columnName = "account_type")
    String type;

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
