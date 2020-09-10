package io.famartin.warehouse.common;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@Path("/stocks")
@RegisterRestClient
public interface StocksClient {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<StockRecord> status();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<StockRecord> update(StockRecord request);

}