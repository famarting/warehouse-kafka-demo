package io.famartin.warehouse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.famartin.warehouse.common.StockRecord;
import io.famartin.warehouse.common.StocksClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

/**
 * StocksService
 */
@ApplicationScoped
public class StocksService {

    // private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    @RestClient
    StocksClient stocksClient;

    public Uni<StockRecord> requestStock(String requestId, String itemId, int quantity) {
        return send(requestId, itemId, quantity, "SUBSTRACT");
    }
 
    private Uni<StockRecord> send(String requestId, String itemId, int quantity, String action) {
        StockRecord record = new StockRecord();
        record.setStockRecordId(UUID.randomUUID().toString());
        record.setItemId(itemId);
        record.setQuantity(quantity);
        record.setAction(action);
        return stocksClient.update(record);
    }

}