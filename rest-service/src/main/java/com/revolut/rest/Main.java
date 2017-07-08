package com.revolut.rest;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/myapp/";

    /*static ArrayList<Account> accounts = new ArrayList<Account>();

    private static void initAccounts() {
        Account acc1 = new Account();
        acc1.setAccountNumber(111111111111111111L);
        acc1.setAmount(200.00);
        acc1.setPersonId(13L);
        acc1.setPersonName("Vasya");

        Account acc2 = new Account();
        acc2.setAccountNumber(222222222222222222L);
        acc2.setAmount(3000.00);
        acc2.setPersonId(232321L);
        acc2.setPersonName("Valariya");

        accounts.add(acc1);
        accounts.add(acc2);
    }
    */

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.revolut.rest package
        final ResourceConfig rc = new ResourceConfig().packages("com.revolut.service");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }


    //public static ArrayList<Account> getAccounts() {
    //    return accounts;
    //}

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));


        //initAccounts();
        //DbManager.startDb();
        //System.in.read();
        //server.stop();
    }
}

