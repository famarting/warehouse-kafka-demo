package io.famartin.warehouse.mongo;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

@ApplicationScoped
public class StocksRepository implements PanacheMongoRepository<StockRecord> {
    
    public Optional<StockRecord> findItem(String itemId) {
        return find("itemId", itemId).firstResultOptional();
    }

}