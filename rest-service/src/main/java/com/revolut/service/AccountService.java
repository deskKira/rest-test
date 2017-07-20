package com.revolut.service;

import com.revolut.db.DbManager;
import com.revolut.model.Account;
import com.revolut.model.TransferResponse;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by monster on 08.07.17.
 */
public class AccountService {

    private static final Logger logger = Logger.getLogger(AccountService.class.getName());
    private static AccountService service = new AccountService();
    private static DbManager dbManager;

    private AccountService() {
        try {
            dbManager = new DbManager();
        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    public static List<Account> getAll() throws SQLException {
        return dbManager.getAll();
    }

    public static Account getItem(Long id) throws SQLException {
        return dbManager.getItem(id);
    }

    public static TransferResponse transfer(Long acc1, Long acc2, String currency, Long amount) throws SQLException {
        //TODO
        return null;
    }
}
