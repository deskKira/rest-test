package com.revolut.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.revolut.model.Account;

import java.sql.SQLException;
import java.util.List;

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

}
