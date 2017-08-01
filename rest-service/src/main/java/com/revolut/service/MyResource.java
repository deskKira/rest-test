package com.revolut.service;

import com.revolut.model.Account;
import com.revolut.model.TransferResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.sql.SQLException;
import java.util.List;

@Path("service")
public class MyResource {

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAccounts() throws Exception {
        return AccountService.getAll();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("id") Long accId) throws Exception {
        return AccountService.getItem(accId);
    }

    @GET
    @Path("/transfer/from/{acc1}/to/{acc2}/amount/{amount}")
    @Produces(MediaType.APPLICATION_JSON)
    public TransferResponse transfer(@PathParam("acc1") Long acc1,
                                     @PathParam("acc2") Long acc2,
                                     @PathParam("amount") Long amount) throws Exception {
        return AccountService.transfer(acc1, acc2, amount);
    }

}
