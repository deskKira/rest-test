package com.revolut.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.revolut.model.Account;
import com.revolut.model.ResponseCode;
import com.revolut.model.TransferResponse;

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

    public TransferResponse transfer(Long acc1, Long acc2, String currency, Long amount) throws SQLException {
        if (checkAmount(acc1, amount)) {
            doTransfer(acc1, acc2, currency, amount);
            return generateResponse(ResponseCode.OK);
        } else {
            //TODO check other accounts, convert and transfer
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("accOwnerName", acc1);
            List<Account> listAcc = dao.queryForFieldValues(map);

        }
        return null;
    }

    private boolean checkAmount(Long acc1, Long amount) throws SQLException {
        Account acc = dao.queryForId(acc1);
        if (acc.getAmount().compareTo(amount) < 0) { return false; }
        else { return true; }
    }

    private void doTransfer(Long acc1, Long acc2, String currency, Long amount) throws SQLException {
        Account account1 = dao.queryForId(acc1);
        Account account2 = dao.queryForId(acc2);

        account1.setAmount(account1.getAmount().longValue() - amount.longValue());
        account2.setAmount(account2.getAmount().longValue() + amount.longValue());

        dao.setAutoCommit(source.getReadWriteConnection(), false);
        dao.update(account1);
        dao.update(account2);
        dao.commit(source.getReadWriteConnection());
    }

    private TransferResponse generateResponse(ResponseCode code) {
        TransferResponse resp = new TransferResponse();
        resp.setResponseCode(code.getStatusCode());
        resp.setResponseMessage(code.name());
        return resp;
    }

}
