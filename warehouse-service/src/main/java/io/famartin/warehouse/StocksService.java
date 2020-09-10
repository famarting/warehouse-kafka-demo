package io.famartin.warehouse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.famartin.warehouse.common.StockRecord;
import io.famartin.warehouse.common.StocksClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
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

    public List<StockRecord> status() {
        return stocksClient.status();
    }

    public Uni<StockRecord> addStock(String requestId, String itemId, int quantity) {
        return send(requestId, itemId, quantity, "ADD");
    }
 
    private Uni<StockRecord> send(String requestId, String itemId, int quantity, String action) {
        StockRecord record = new StockRecord();
        record.setItemId(itemId);
        record.setQuantity(quantity);
        record.setAction(action);
        return stocksClient.update(record);
    }

}