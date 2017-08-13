package com.revolut.runner;

import com.revolut.db.DbManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.sqlite.SQLiteConnection;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.sql.Statement;

import static com.revolut.db.Scripts.*;
import static com.revolut.db.Scripts.DATA_ACC5;

public class ServiceRunner {

    public static final String BASE_URI = "http://localhost:8080/myapp/";
    private static final String DB_URI = "jdbc:sqlite";
    private static final String DB_FILE_NAME = "main.db";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.revolut.service");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws Exception {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n", BASE_URI));
        initDb();
    }

    private static void initDb() throws SQLException {
        DbManager manager = new DbManager();
        File file = new File(getPathToDb());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                System.out.println("Created DB file by path:"+getPathToDb());
                SQLiteConnection connection = manager.getDbConnection();
                //connection = new SQLiteConnection(DB_URI, DB_FILE_NAME);
                Statement statement = connection.createStatement();
                statement.addBatch(CREATE_TABLE_QUERY);
                statement.addBatch(DATA_ACC1);
                statement.addBatch(DATA_ACC2);
                statement.addBatch(DATA_ACC3);
                statement.addBatch(DATA_ACC4);
                statement.addBatch(DATA_ACC5);

                statement.executeBatch();
                statement.close();
                manager.closeDbConnection(connection);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("DB file already exist by path:"+getPathToDb());
        }
    }

    public static String getPathToDb() {
        StringBuilder pathToDbFile = new StringBuilder();
        pathToDbFile.append(System.getProperty("user.home"))
                .append(File.separator).append("revolut_test").append(File.separator)
                .append(DB_FILE_NAME);
        return pathToDbFile.toString();
    }
}

