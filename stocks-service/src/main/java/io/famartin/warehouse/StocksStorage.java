package io.famartin.warehouse;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.famartin.warehouse.mongo.StockRecord;
import io.famartin.warehouse.mongo.StocksRepository;
import io.smallrye.mutiny.tuples.Tuple2;

@Singleton
public class StocksStorage {
    
    private ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    @Inject
    StocksRepository stocksRepo;

    @ConfigProperty(name = "STORAGE", defaultValue = "MONGO")
    String storageType;

    Object lock = new Object();

    public Stream<StockRecord> streamAll() {
        return stocksRepo.streamAll();
    }

    public Integer addStock(String itemId, int quantity) {
        if (storageType.equals("MEMORY")) {
            Integer newStock = stock.compute(itemId, (id, currentStock) -> {
                if ( currentStock == null ) {
                    return quantity;
                } else {
                    return currentStock + quantity;
                }
            });
            return newStock;
        } else {
            synchronized (lock) {
                StockRecord record = new StockRecord();
                record.itemId = itemId;
                Optional<StockRecord> recordOpt = stocksRepo.findItem(itemId);
                if (recordOpt.isPresent()) {
                    record.id = recordOpt.get().id;
                    record.stock = recordOpt.get().stock + quantity;
                    stocksRepo.update(record);
                } else {
                    record.stock = quantity;
                    stocksRepo.persist(record);
                }
                stocksRepo.persistOrUpdate(record);
                return record.stock;
            }
        }
    }

    public Tuple2<Boolean, Integer> substractStock(String itemId, int quantity) {
        if (storageType.equals("MEMORY")) {
            AtomicBoolean approved = new AtomicBoolean(false);
            Integer newStock = stock.computeIfPresent(itemId, (id, currentStock) -> {
                if(currentStock >= quantity) {
                    approved.set(true);
                    return currentStock - quantity;
                } else {
                    approved.set(false);
                    return currentStock;
                }
            });
            return Tuple2.of(approved.get(), newStock);
        } else {
            synchronized (lock) {
                StockRecord record = new StockRecord();
                record.itemId = itemId;
                Optional<StockRecord> recordOpt = stocksRepo.findItem(itemId);
                if (recordOpt.isPresent()) {
                    if(recordOpt.get().stock >= quantity) {
                        record.id = recordOpt.get().id;
                        record.stock = recordOpt.get().stock - quantity;
                        stocksRepo.update(record);
                        return Tuple2.of(true, record.stock);
                    } else {
                        return Tuple2.of(false, recordOpt.get().stock);
                    }
                } else {
                    return Tuple2.of(false, null);
                }
            }
        }
    }

}