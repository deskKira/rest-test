package com.revolut.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.*;

@Path("service")
public class MyResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @GET
    @Path("/all")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccounts() throws Exception {
        return AccountService.getAll().stream().map(c -> c.toString()).collect(joining("\n"));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccount(@PathParam("id") Long personId) throws Exception {
        return AccountService.getItem(personId) == null ?
                String.format("Sorry, but user with person id= %s not found", personId):
                AccountService.getItem(personId).toString();
    }

}
