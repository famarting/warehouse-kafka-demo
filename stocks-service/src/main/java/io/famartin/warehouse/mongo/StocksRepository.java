package io.famartin.warehouse.mongo;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.internal.operation.AggregateOperation;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class StocksRepository { //implements PanacheMongoRepository<StockRecord> {
    
    @Inject
    MongoCollection<Document> stocksCollection;

    public Stream<StockRecord> streamAll() {
        // stocksCollection.find().map(doc -> {
        //     StockRecord rec = new StockRecord();
        //     // rec.itemId = doc.get("itemId");
        //     return rec;
        // })
        // .spliterator();
        return StreamSupport.stream(
            stocksCollection.find().map(doc -> {
                StockRecord rec = new StockRecord();
                rec.itemId = doc.getString("itemId");
                rec.stock = doc.getInteger("stock");
                return rec;
            }).spliterator(), 
            false);
    }

    // update

    public void update(StockRecord record) {
        stocksCollection.updateOne(
            Filters.eq("itemId", record.itemId),
            Updates.combine(Updates.set("stock", record.stock)));
    }

    // persist
    public void persist(StockRecord record) {
        Document doc = new Document("itemId", record.itemId)
            .append("stock", record.stock);
        stocksCollection.insertOne(doc);
    }

    // persistOrUpdate
    public void persistOrUpdate(StockRecord record) {
        if (stocksCollection.find(
                Filters.eq("itemId", record.itemId)).iterator().hasNext()) {
            update(record);
        } else {
            persist(record);
        }
    }

    public Optional<StockRecord> findItem(String itemId) {
        // return find("itemId", itemId).firstResultOptional();
        MongoCursor<Document> cursor = stocksCollection.find(
                Filters.eq("itemId", itemId)).iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            StockRecord rec = new StockRecord();
            rec.itemId = doc.getString("itemId");
            rec.stock = doc.getInteger("stock");
            return Optional.of(rec);
        }
        return Optional.empty();
    }

}