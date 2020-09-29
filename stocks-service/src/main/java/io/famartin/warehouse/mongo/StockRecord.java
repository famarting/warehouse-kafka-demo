package io.famartin.warehouse.mongo;

import org.bson.types.ObjectId;


// @MongoEntity(collection = "stockrecord")
public class StockRecord {
    public ObjectId id;
    public String itemId;
    public Integer stock;
}