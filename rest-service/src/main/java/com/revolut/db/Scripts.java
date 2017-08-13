package com.revolut.db;

public interface Scripts {

    public static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS 'account' ( `acc_id` NUMERIC NOT NULL, `acc_owner_name` TEXT, `account_number` NUMERIC, `amount` NUMERIC, `account_type` TEXT, `acc_user_id` NUMERIC, PRIMARY KEY(`acc_id`) )";
    public static final String TRUNCATE_ACCOUNT_QUERY = "TRUNCATE TABLE account";
    public static final String LIST_ACCOUNT_QUERY = "select acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id from account";
    public static final String ITEM_ACCOUNT_QUERY = "select acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id from account where account_number = ?";
    public static final String ACCOUNTS_BY_USER_ID = "select acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id from account where acc_user_id = ?";
    public static final String UPDATE_AMOUNT_BY_ACC_NUMBER = "update account set amount = ? where account_number = ?";
    public static final String DATA_ACC1 = "insert into account (acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id) values (1, 'Vasya Kupcov',222222222222222222,30000,'EUR',11)";
    public static final String DATA_ACC2 = "insert into account (acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id) values (2, 'Ivan Demidov',111111111111111111,24000,'USD',12)";
    public static final String DATA_ACC3 = "insert into account (acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id) values (3, 'Natali Lafore' ,111111111111111123,10000,'RUB',31)";
    public static final String DATA_ACC4 = "insert into account (acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id) values (4, 'Vasya Kupcov',222222222222222223,23000,'USD',11)";
    public static final String DATA_ACC5 = "insert into account (acc_id,acc_owner_name,account_number,amount,account_type,acc_user_id) values (5, 'Natali Lafore',111111111111111127, 20000,'USD',31)";

    public static final String DEL_ACC = "delete from account where 1 = 1";

    }
