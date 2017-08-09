package com.revolut.rest.db;

import com.revolut.db.DbManager;
import com.revolut.model.Account;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sqlite.SQLiteConnection;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.revolut.db.Scripts.*;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DbManagerTest {

    static SQLiteConnection connection;

    @BeforeClass
    public static void initDbFile() throws IOException, SQLException {
        System.out.println("before class");
        File file = new File("main_test.db");
        file.createNewFile();
        connection = new SQLiteConnection("jdbc:sqlite", "main_test.db");
        Statement statement = connection.createStatement();
        statement.execute(CREATE_TABLE_QUERY);
        statement.close();
    }

    @Before
    public void beforeSetUp() throws SQLException {
        System.out.println("before test");
        Statement statement = connection.createStatement();
        statement.addBatch(DATA_ACC1);
        statement.addBatch(DATA_ACC2);
        statement.addBatch(DATA_ACC3);
        statement.addBatch(DATA_ACC4);
        statement.executeBatch();
        statement.close();

    }

    @Test
    public void getAllTest() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(LIST_ACCOUNT_QUERY);
        Account account1 = new Account(1L, "Vasya Kupcov",222222222222222222L,30000L,"EURO",11L);
        Account account2 = new Account(2L, "Ivan Demidov",111111111111111111L,24000L,"DOLLAR",12L);
        Account account3 = new Account(3L, "Natali Lafore" ,111111111111111123L,10000L,"RUBLE",31L);
        Account account4 = new Account(4L, "Vasya Kupcov",222222222222222223L,23000L,"DOLLAR",11L);

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

        rs.close();
        statement.close();

        assertEquals(account1.getAccountNumber(), accounts.get(0).getAccountNumber());
        assertEquals(account2.getAccountNumber(), accounts.get(1).getAccountNumber());
        assertEquals(account3.getAccountNumber(), accounts.get(2).getAccountNumber());
        assertEquals(account4.getAccountNumber(), accounts.get(3).getAccountNumber());
    }

    @Test
    public void getItemTest() throws SQLException {
        Account account = new Account(4L, "Vasya Kupcov",222222222222222223L,23000L,"DOLLAR",11L);
        PreparedStatement statement = connection.prepareStatement(ITEM_ACCOUNT_QUERY);
        statement.setLong(1,account.getAccountNumber());
        ResultSet rs = statement.executeQuery();

        assertEquals(account.getAccOwnerName(), rs.getString("acc_owner_name"));
        assertEquals(account.getAmount().longValue(), rs.getLong("amount"));
        assertEquals(account.getType(), rs.getString("account_type"));
        assertEquals(account.getAccUserId().longValue(), rs.getLong("acc_user_id"));

        statement.close();
        rs.close();

    }

    @Test
    public void testAccToAccWithOneCurrency() throws SQLException {
        DbManager manager = new DbManager("main_test.db");
        Account account2 = new Account(2L, "Ivan Demidov",111111111111111111L,24000L,"DOLLAR",12L);
        Account account4 = new Account(4L, "Vasya Kupcov",222222222222222223L,23000L,"DOLLAR",11L);

        manager.transfer(account2.getAccountNumber(), account4.getAccountNumber(), 3000L);
        Account acc2 = manager.getAccount(account2.getAccountNumber());
        Account acc4 = manager.getAccount(account4.getAccountNumber());

        assertEquals(21000L, acc2.getAmount().longValue());
        assertEquals(26000L, acc4.getAmount().longValue());
    }

    @Test
    public void testAccToAccWithDifferentCurrency() throws SQLException {
        DbManager manager = new DbManager("main_test.db");
        Account testAccount1 = new Account(1L, "Vasya Kupcov",222222222222222222L,30000L,"EURO",11L);
        Account testAccount2 = new Account(2L, "Ivan Demidov",111111111111111111L,24000L,"DOLLAR",12L);
        manager.transfer(testAccount1.getAccountNumber(), testAccount2.getAccountNumber(), 10000L);

        Account account1 = manager.getAccount(testAccount1.getAccountNumber());
        Account account2 = manager.getAccount(testAccount2.getAccountNumber());

        //assertEquals(20000L, account1.getAmount().longValue());
        assertEquals(35664L, account2.getAmount().longValue());
    }

    @After
    public void afterSetUp() throws SQLException {
        System.out.println("after test");
        Statement statement = connection.createStatement();
        statement.execute(DEL_ACC);
        statement.close();
    }

    @AfterClass
    public static void destroyDbFile() throws SQLException {
        Statement statement = connection.createStatement();
        statement.close();
        connection.close();
        File file = new File("main_test.db");
        if (file.exists()) { file.delete(); }
    }
}
