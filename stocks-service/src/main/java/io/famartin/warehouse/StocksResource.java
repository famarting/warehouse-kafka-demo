package io.famartin.warehouse;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.famartin.warehouse.common.EventsService;
import io.famartin.warehouse.common.StockRecord;
import io.smallrye.mutiny.tuples.Tuple2;

@ApplicationScoped
@Path("/stocks")
public class StocksResource {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    EventsService events;

    @Inject
    StocksStorage stocksStorage;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<StockRecord> status() {
        return stocksStorage.streamAll().map(r -> {
            StockRecord s = new StockRecord();
            s.setItemId(r.itemId);
            s.setQuantity(r.stock);
            return s;
        })
        .sorted()
        .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public StockRecord update(StockRecord request) {
        if(request.getAction()!=null && request.getItemId()!=null && request.getQuantity()!=null && request.getQuantity()>0) {
            String action = request.getAction();
            String itemId = request.getItemId();
            int quantity = request.getQuantity();
            try{
                StockRecord response = new StockRecord();
                switch (StockAction.valueOf(action)) {
                    case ADD:
                        Integer newStock = stocksStorage.addStock(itemId, quantity);
                        events.sendEvent("Stock updated, item: "+itemId+" quantity: "+newStock);
                        events.sendStockEvent(itemId, newStock);
                        break;
                    case SUBSTRACT:
                        Tuple2<Boolean, Integer> substractResult =  stocksStorage.substractStock(itemId, quantity);
                        response.setApproved(substractResult.getItem1());
                        if (!substractResult.getItem1()) {
                            response.setMessage("Stock request exceeded current stock");
                            response.setOriginalRequest(request.toString());
                        }
                        Integer result = substractResult.getItem2();
                        if (result == null) {
                            response.setMessage("There is no stock for that item");
                        } else if (result == 0) {
                            events.sendEvent("Item "+itemId+" ran out of stock");
                        } else {
                            events.sendEvent("Stock updated, item: "+itemId+" quantity: "+result);
                        }
                        if (response.getApproved()) {
                            events.sendStockEvent(itemId, result);
                        }
                        break;
                }
                response.setTimestamp(Instant.now().toString());
                return response;
            } catch (IllegalArgumentException e) {
                logger.error("Bad request", e);
                StockRecord response = new StockRecord();
                response.setError(String.format("Bad request, action %s not exists", action));
                response.setOriginalRequest(request.toString());
                return response;
            }
        } else {
            StockRecord response = new StockRecord();
            response.setError("Bad request, bad fields");
            response.setOriginalRequest(request.toString());
            return response;
        }
    }

}