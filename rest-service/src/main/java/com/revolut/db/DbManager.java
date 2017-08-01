package com.revolut.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.revolut.model.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by monster on 07.07.17.
 */
public class DbManager {
    private final String url = "jdbc:sqlite:main";
    private ConnectionSource source;
    private Dao<Account, Long> dao;

    public DbManager() throws SQLException {
        source = new JdbcConnectionSource(url);
        dao = DaoManager.createDao(source, Account.class);
    }

    public List<Account> getAll() throws SQLException {
        return dao.queryForAll();
    }

    public Account getItem(Long id) throws SQLException {
        return dao.queryForId(id);
    }

    public TransferResponse transfer(Long acc1, Long acc2, Long amount) {
        try {
            if (checkThatSingleCurrency(acc1, acc2)) {
                if (checkAmount(acc1, amount)) {
                    doTransfer(acc1, acc2, amount);
                    return generateResponse(ResponseCode.OK);
                } else {
                    Map<Account, Long> accounts = checkOtherClientAccounts(acc1, amount);
                    doTransfer(accounts, acc2);
                    return generateResponse(ResponseCode.OK);
                }
            } else {
                Map<Account, Long> accounts = checkOtherClientAccounts(acc1, amount);
                doTransfer(accounts, acc2);
                return generateResponse(ResponseCode.OK);
            }
        } catch (Exception e) {
            System.err.println("transfer error: " + e);
            return generateResponse(ResponseCode.ERROR);
        }
    }

    private Map<Account, Long> checkOtherClientAccounts(Long writeOffAccount, Long amount) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Account account = dao.queryForId(writeOffAccount);
        map.put("accUserId", account.getAccUserId());

        List<Account> accountsByUser = dao.queryForFieldValues(map);

        Map<Account, Long> sumAmountsByAccounts = chechThatFundsSufficient(accountsByUser, account, amount);
        if (isEnoughMoney(sumAmountsByAccounts, account, amount)) {
            return sumAmountsByAccounts;
        } else {
            throw new Exception("insufficient funds error");
        }
    }

    private boolean checkThatSingleCurrency(Long acc1, Long acc2) throws SQLException {
        Account writeOffAccount = dao.queryForId(acc1);
        Account writeOnAccount = dao.queryForId(acc2);
        if (!writeOffAccount.getType().equals(writeOnAccount)) { return false; }
        return true;
    }

    private Map<Account, Long> chechThatFundsSufficient(List<Account> accounts, Account writeOffAccount, Long amount) throws Exception {
        String typeAcc = writeOffAccount.getType();
        Map<Account, Long> withdrawalFromAccounts = new HashMap<Account, Long>();

        Long totalAmount = amount;
        for (Account account : accounts) {
            Long value = convertCurrency(AccountType.valueOf(typeAcc).getCurrencyCode(), AccountType.valueOf(account.getType()).getCurrencyCode(), amount);
            if (account.getAmount().compareTo(value) >= 0) {
                withdrawalFromAccounts.put(account, value);
                break;
            } else {
                long remain = value.longValue() - account.getAmount().longValue();
                withdrawalFromAccounts.put(account, value.longValue() - remain);
                amount = remain;
                continue;
            }

        }
        return withdrawalFromAccounts;
    }


    private boolean isEnoughMoney(Map<Account, Long> withdrawalFromAccounts, Account writeOffAmount, Long totalAmount) throws Exception {
        long sumAmounts = 0L;
        for (Map.Entry<Account, Long> entry : withdrawalFromAccounts.entrySet()) {
             sumAmounts += convertCurrency(AccountType.valueOf(entry.getKey().getType()).getCurrencyCode(), AccountType.valueOf(writeOffAmount.getType()).getCurrencyCode(), totalAmount);
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
                break;
            case 840:
                if (AccountType.RUBLE.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.USDTORUB.getFactor());
                }
                else if (AccountType.EURO.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.USDTOEURO.getFactor());
                }
                break;
            case 643:
                if (AccountType.EURO.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.RUBTOEURO.getFactor());
                }
                else if (AccountType.DOLLAR.getCurrencyCode() == toCurrCode) {
                    resultAmount = Math.round(amount * CrossCourceCurrency.RUBTOUSD.getFactor());
                }
                break;
            default:
                throw new Exception("Illegal currency");

        }
        return resultAmount;
    }

    private boolean checkAmount(Long acc1, Long amount) throws SQLException {
        Account acc = dao.queryForId(acc1);
        if (acc.getAmount().compareTo(amount) < 0) { return false; }
        else { return true; }
    }

    private void doTransfer(Long acc1, Long acc2, Long amount) throws Exception {
        Account account1 = dao.queryForId(acc1);
        Account account2 = dao.queryForId(acc2);

        account1.setAmount(account1.getAmount().longValue() - amount.longValue());
        account2.setAmount(account2.getAmount().longValue() + amount.longValue());

        try {
            dao.setAutoCommit(source.getReadWriteConnection(), false);
            dao.update(account1);
            dao.update(account2);
            dao.commit(source.getReadWriteConnection());
        } catch (SQLException e) {
            System.err.println("Error with code:" +e.getErrorCode());
            throw new Exception(e);
        } finally {
            dao.rollBack(source.getReadWriteConnection());
        }

    }

    private void doTransferWithoutAutoCommit(Account acc1, Long account2, Long amount) throws Exception {
        Account acc2 = dao.queryForId(account2);
        acc1.setAmount(acc1.getAmount().longValue() - amount.longValue());
        acc2.setAmount(acc2.getAmount().longValue() + amount.longValue());
        try {
            dao.update(acc1);
            dao.update(acc2);
        } catch (SQLException e) {
            System.err.println("Error with code:" +e.getErrorCode());
            throw new Exception(e);
        }
    }

    private void doTransfer(Map<Account, Long> fromAccounts, Long toAccount) throws Exception {
        try {
            dao.setAutoCommit(source.getReadWriteConnection(), false);
            for (Map.Entry<Account, Long> entry : fromAccounts.entrySet()) {
                doTransferWithoutAutoCommit(entry.getKey(), toAccount, entry.getValue());
            }
            dao.commit(source.getReadWriteConnection());
        } catch (Exception e) {
            System.err.println("Error transfer process!");
            throw new Exception(e);
        } finally {
            dao.rollBack(source.getReadWriteConnection());
        }

    }

    private TransferResponse generateResponse(ResponseCode code) {
        TransferResponse resp = new TransferResponse();
        resp.setResponseCode(code.getStatusCode());
        resp.setResponseMessage(code.name());
        return resp;
    }

}
