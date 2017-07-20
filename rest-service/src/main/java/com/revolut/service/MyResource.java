package com.revolut.service;

import com.revolut.model.Account;
import com.revolut.model.TransferResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @Path("/from/{acc1}/to/{acc2}/currency/{cur}/amount/{amount}")
    @Produces(MediaType.APPLICATION_JSON)
    public TransferResponse transfer(@PathParam("acc1") Long acc1,
                                     @PathParam("acc2") Long acc2,
                                     @PathParam("cur") String cur,
                                     @PathParam("amount") Long amount) {
        //TODO
        return null;
    }

}
