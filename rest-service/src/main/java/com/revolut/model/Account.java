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

    @DatabaseField(generatedId = true, columnName = "person_id")
    Long personId;
    @DatabaseField(columnName = "person_name")
    String personName;
    @DatabaseField(columnName = "account_number")
    Long accountNumber;
    @DatabaseField(columnName = "amount")
    Double amount;

    @Override
    public String toString() {
        return "Account{" +
                "personId=" + personId +
                ", personName='" + personName + '\'' +
                ", accountNumber=" + accountNumber +
                ", amount=" + amount +
                '}';
    }
}
