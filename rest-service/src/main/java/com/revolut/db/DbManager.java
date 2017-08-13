package com.revolut.db;

import com.revolut.model.*;
import org.sqlite.SQLiteConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.revolut.db.Scripts.*;

/**
 * Created by monster on 07.07.17.
 */
public class DbManager {

    private static final Logger log = Logger.getLogger(DbManager.class.getName());

    private final String url = "jdbc:sqlite";
    private String fileName = "main";

    public DbManager() {

    }

    public DbManager(String dbFileName) {
        fileName = dbFileName;
    }

    public SQLiteConnection getDbConnection() throws SQLException {
        SQLiteConnection connection = new SQLiteConnection(url, fileName);
        return connection;
    }

    public void closeDbConnection(SQLiteConnection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public List<Account> getAllAccounts() throws SQLException {
        SQLiteConnection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = getDbConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(LIST_ACCOUNT_QUERY);

            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                Account account = new Account();
                account.setAccId(rs.getLong("acc_id"));
                account.setAccountNumber(rs.getLong("account_number"));
                account.setAmount(rs.getLong("amount"));
                account.setAccOwnerName(rs.getString("acc_owner_name"));
                account.setAccUserId(rs.getLong("acc_user_id"));
                account.setType(rs.getString("account_type"));
                accounts.add(account);
            }
            return accounts;
        } catch (Exception e) {
            log.info("Error when calling getAll() accounts");
            throw new SQLException(e);
        } finally {
            rs.close();
            statement.close();
            closeDbConnection(connection);
        }

    }

    public Account getAccount(Long id) throws SQLException {
        SQLiteConnection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = getDbConnection();
            statement = connection.prepareStatement(ITEM_ACCOUNT_QUERY);
            statement.setLong(1, id.longValue());
            rs = statement.executeQuery();
            Account account = new Account();
            if (rs.next()) {
                account.setAccId(rs.getLong("acc_id"));
                account.setAccountNumber(rs.getLong("account_number"));
                account.setAmount(rs.getLong("amount"));
                account.setAccOwnerName(rs.getString("acc_owner_name"));
                account.setAccUserId(rs.getLong("acc_user_id"));
                account.setType(rs.getString("account_type"));

            }
            return account;
        } catch (Exception e) {
            log.info("error:"+e.getMessage());
            throw new SQLException(e);
        } finally {
            rs.close();
            statement.close();
            closeDbConnection(connection);
        }
    }

    public List<Account> getAccountsByUserId(Long userId) throws SQLException {
        SQLiteConnection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Account> accounts = null;
        try {
            connection = getDbConnection();
            statement = connection.prepareStatement(ACCOUNTS_BY_USER_ID);
            statement.setLong(1, userId.longValue());
            rs = statement.executeQuery();
            accounts = new ArrayList<Account>();
            while (rs.next()) {
                Account account = new Account();
                account.setAccId(rs.getLong("acc_id"));
                account.setAccountNumber(rs.getLong("account_number"));
                account.setAmount(rs.getLong("amount"));
                account.setAccOwnerName(rs.getString("acc_owner_name"));
                account.setAccUserId(rs.getLong("acc_user_id"));
                account.setType(rs.getString("account_type"));
                accounts.add(account);
            }
            return accounts;
        } catch (Exception e) {
            log.info("error:"+ e.getMessage());
            throw new SQLException(e);
        } finally {
            rs.close();
            statement.close();
            closeDbConnection(connection);
        }
    }

    public Statement updateAmountByAccount(SQLiteConnection connection, Long amount, Long account) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(UPDATE_AMOUNT_BY_ACC_NUMBER);
            statement.setLong(1, amount.longValue());
            statement.setLong(2, account.longValue());
            statement.execute();
            return statement;
        } catch (Exception e) {
            log.info("error:"+ e.getMessage());
            throw new SQLException(e);
        } finally {
            statement.close();
        }
    }

    public TransferResponse transfer(Long acc1, Long acc2, Long amount) {
        try {
            if (checkThatSingleCurrency(acc1, acc2)) {
                if (checkAmount(acc1, amount)) {
                    doTransfer(acc1, acc2, amount);
                    return generateResponse(ResponseCode.OK);
                } else {
                    Map<Account, Long> accounts = checkOtherClientAccounts(acc1, acc2, amount);
                    doTransfer(accounts, acc2);
                    return generateResponse(ResponseCode.OK);
                }
            } else {
                Map<Account, Long> accounts = checkOtherClientAccounts(acc1, acc2, amount);
                doTransfer(accounts, acc2);
                return generateResponse(ResponseCode.OK);
            }
        } catch (Exception e) {
            System.err.println("transfer error: " + e);
            return generateResponse(ResponseCode.ERROR);
        }
    }

    private Map<Account, Long> checkOtherClientAccounts(Long writeOffAccount, Long writeOnAmount, Long amount) throws Exception {
        Long totalAmount = amount;
        Account account = getAccount(writeOffAccount);
        Account accountOn = getAccount(writeOnAmount);
        List<Account> accountsByUser = getAccountsByUserId(account.getAccUserId());

        Map<Account, Long> sumAmountsByAccounts = chechThatFundsSufficient(accountsByUser, account, amount);
        if (isEnoughMoney(sumAmountsByAccounts, account, totalAmount)) {
            return sumAmountsByAccounts;
        } else {
            throw new Exception("insufficient funds error");
        }
    }

    private boolean checkThatSingleCurrency(Long acc1, Long acc2) throws SQLException {
        Account writeOffAccount = getAccount(acc1);
        Account writeOnAccount = getAccount(acc2);
        if (!writeOffAccount.getType().equals(writeOnAccount.getType())) { return false; }
        return true;
    }

    private Map<Account, Long> chechThatFundsSufficient(List<Account> accounts, Account writeOffAccount, Long amount) throws Exception {
        Map<Account, Long> withdrawalFromAccounts = new HashMap<Account, Long>();
        
        for (Account account : accounts) {
            Long value = convertCurrency(AccountType.valueOf(account.getType()).getCurrencyCode(), AccountType.valueOf(writeOffAccount.getType()).getCurrencyCode(), account.getAmount().longValue());

            if (writeOffAccount.getType().equals(account.getType())) {
                if (value.compareTo(amount) >= 0) {
                    withdrawalFromAccounts.put(account, amount);
                    break;
                } else {
                    long remain = amount.longValue() - value.longValue();
                    withdrawalFromAccounts.put(account, amount.longValue() - remain);
                    amount = changeAmountValue(amount, remain);
                    continue;
                }
            } else {
                if (value.compareTo(amount) >= 0) {
                    withdrawalFromAccounts.put(account, convertCurrency(AccountType.valueOf(writeOffAccount.getType()).getCurrencyCode(), AccountType.valueOf(account.getType()).getCurrencyCode(), amount));
                    break;
                } else {
                    long remain = amount.longValue() - value.longValue();
                    withdrawalFromAccounts.put(account, convertCurrency(AccountType.valueOf(writeOffAccount.getType()).getCurrencyCode(), AccountType.valueOf(account.getType()).getCurrencyCode(), amount.longValue() - remain));
                    amount = changeAmountValue(amount, remain);
                    continue;
                }
            }
        }
        return withdrawalFromAccounts;
    }

    private Long changeAmountValue(Long amount, long remain) {
        amount = Long.valueOf(remain);
        return amount;
    }

    private boolean isEnoughMoney(Map<Account, Long> withdrawalFromAccounts, Account writeOnAmount, Long totalAmount) throws Exception {
        long sumAmounts = 0L;
        for (Map.Entry<Account, Long> entry : withdrawalFromAccounts.entrySet()) {
             sumAmounts += convertCurrency(AccountType.valueOf(entry.getKey().getType()).getCurrencyCode(), AccountType.valueOf(writeOnAmount.getType()).getCurrencyCode(), entry.getKey().getAmount());
        }
        if (sumAmounts >= totalAmount.longValue()) {
            return true;
        } else {
            return false;
        }
    }

    private Long convertCurrency(int fromCurrCode, int toCurrCode, long amount) throws Exception {
        long resultAmount = 0L;
        switch (fromCurrCode) {
            case 978:
                if (AccountType.RUBLE.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.EUROTORUB.getFactor());
                }
                else if (AccountType.DOLLAR.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.EUROTOUSD.getFactor());
                }
                else if (AccountType.EURO.getCurrencyCode() == toCurrCode) {
                    resultAmount = amount;
                }
                break;
            case 840:
                if (AccountType.RUBLE.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.USDTORUB.getFactor());
                }
                else if (AccountType.EURO.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.USDTOEURO.getFactor());
                }
                else if (AccountType.DOLLAR.getCurrencyCode() == toCurrCode) {
                    resultAmount = amount;
                }
                break;
            case 643:
                if (AccountType.EURO.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.RUBTOEURO.getFactor());
                }
                else if (AccountType.DOLLAR.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.RUBTOUSD.getFactor());
                }
                else if (AccountType.RUBLE.getCurrencyCode() == toCurrCode) {
                    resultAmount = amount;
                }
                break;
            default:
                throw new Exception("Illegal currency");

        }
        return resultAmount;
    }

    private boolean checkAmount(Long acc1, Long amount) throws SQLException {
        Account acc = getAccount(acc1);
        if (acc.getAmount().compareTo(amount) < 0) { return false; }
        else { return true; }
    }

    private void doTransfer(Long acc1, Long acc2, Long amount) throws Exception {
        Account account1 = getAccount(acc1);
        Account account2 = getAccount(acc2);

        account1.setAmount(account1.getAmount().longValue() - amount.longValue());
        account2.setAmount(account2.getAmount().longValue() + amount.longValue());

        SQLiteConnection connection = null;

        try {
            connection = getDbConnection();
            connection.setAutoCommit(false);

            updateAmountByAccount(connection, account1.getAmount().longValue(), account1.getAccountNumber());
            updateAmountByAccount(connection, account2.getAmount().longValue(), account2.getAccountNumber());

            connection.commit();
        } catch (SQLException e) {
            System.err.println("Error with code:" +e.getErrorCode());
            connection.rollback();
            throw new Exception(e);
        } finally {
            closeDbConnection(connection);
        }
    }

    private void doTransferWithoutAutoCommit(SQLiteConnection connection, Account acc1, Long account2, Long amount) throws Exception {
        Account acc2 = getAccount(account2);
        acc1.setAmount(acc1.getAmount().longValue() - amount.longValue());
        acc2.setAmount(acc2.getAmount().longValue() + convertCurrency(AccountType.valueOf(acc1.getType()).getCurrencyCode(), AccountType.valueOf(acc2.getType()).getCurrencyCode(), amount.longValue()).longValue());
        try {
            updateAmountByAccount(connection, acc1.getAmount(), acc1.getAccountNumber());
            updateAmountByAccount(connection, acc2.getAmount(), acc2.getAccountNumber());
        } catch (SQLException e) {
            System.err.println("Error with code:" +e.getErrorCode());
            throw new Exception(e);
        }
    }

    private void doTransfer(Map<Account, Long> fromAccounts, Long toAccount) throws Exception {
        SQLiteConnection connection = null;
        try {
            connection = getDbConnection();
            connection.setAutoCommit(false);
            for (Map.Entry<Account, Long> entry : fromAccounts.entrySet()) {
                doTransferWithoutAutoCommit(connection, entry.getKey(), toAccount, entry.getValue());
            }
            connection.commit();
        } catch (Exception e) {
            System.err.println("Error transfer process!");
            connection.rollback();
            throw new Exception(e);
        } finally {
            closeDbConnection(connection);
        }

    }

    private TransferResponse generateResponse(ResponseCode code) {
        TransferResponse resp = new TransferResponse();
        resp.setResponseCode(code.getStatusCode());
        resp.setResponseMessage(code.name());
        return resp;
    }

}
