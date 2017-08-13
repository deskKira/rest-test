package com.revolut.rest.db;

import com.revolut.db.DbManager;
import com.revolut.model.Account;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sqlite.SQLiteConnection;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
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
        statement.addBatch(DATA_ACC5);

        statement.executeBatch();
        statement.close();

    }

    @Test
    public void getAllTest() throws SQLException {
        DbManager manager = new DbManager("main_test.db");
        Account account1 = new Account(1L, "Vasya Kupcov",222222222222222222L,30000L,"EURO",11L);
        Account account2 = new Account(2L, "Ivan Demidov",111111111111111111L,24000L,"DOLLAR",12L);
        Account account3 = new Account(3L, "Natali Lafore" ,111111111111111123L,10000L,"RUBLE",31L);
        Account account4 = new Account(4L, "Vasya Kupcov",222222222222222223L,23000L,"DOLLAR",11L);
        Account account5 = new Account(5L, "Natali Lafore",111111111111111127L, 20000L,"DOLLAR", 31L);

        List<Account> accounts = manager.getAllAccounts();

        assertEquals(account1.getAccountNumber(), accounts.get(0).getAccountNumber());
        assertEquals(account2.getAccountNumber(), accounts.get(1).getAccountNumber());
        assertEquals(account3.getAccountNumber(), accounts.get(2).getAccountNumber());
        assertEquals(account4.getAccountNumber(), accounts.get(3).getAccountNumber());
        assertEquals(account5.getAccountNumber(), accounts.get(4).getAccountNumber());
    }

    @Test
    public void getItemTest() throws SQLException {
        DbManager manager = new DbManager("main_test.db");
        Account account = new Account(4L, "Vasya Kupcov",222222222222222223L,23000L,"DOLLAR",11L);
        Account result = manager.getAccount(account.getAccountNumber());

        assertEquals(account.getAccOwnerName(), result.getAccOwnerName());
        assertEquals(account.getAmount().longValue(), result.getAmount().longValue());
        assertEquals(account.getType(), result.getType());
        assertEquals(account.getAccUserId().longValue(), result.getAccUserId().longValue());
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

        assertEquals(20000L, account1.getAmount().longValue());
        assertEquals(35664L, account2.getAmount().longValue());
    }

    @Test
    public void testAccToAccWithDiffCurrAndShortageOfFundsOnOneOfThem() throws SQLException {
        DbManager manager = new DbManager("main_test.db");
        Account testAccount2 = new Account(2L, "Ivan Demidov",111111111111111111L,24000L,"DOLLAR",12L);
        Account testAccount3 = new Account(3L, "Natali Lafore" ,111111111111111123L,10000L,"RUBLE",31L);
        Account testAccount5 = new Account(5L, "Natali Lafore",111111111111111127L, 20000L,"DOLLAR", 31L);

        manager.transfer(testAccount3.getAccountNumber(), testAccount2.getAccountNumber(), 20000L);

        Account acc2 = manager.getAccount(testAccount2.getAccountNumber());
        Account acc3 = manager.getAccount(testAccount3.getAccountNumber());
        Account acc5 = manager.getAccount(testAccount5.getAccountNumber());

        assertEquals( 0L, acc3.getAmount().longValue());
        assertEquals(24168L, acc2.getAmount().longValue());
        assertEquals(19832L, acc5.getAmount().longValue());
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
        System.out.println("after class");
        Statement statement = connection.createStatement();
        statement.close();
        connection.close();
        File file = new File("main_test.db");
        if (file.exists()) { file.delete(); }
    }
}
