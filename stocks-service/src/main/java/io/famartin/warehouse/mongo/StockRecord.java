package io.famartin.warehouse.mongo;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.MongoEntity;

@MongoEntity(collection = "stockrecord")
public class StockRecord {
    public ObjectId id;
    public String itemId;
    public Integer stock;
}