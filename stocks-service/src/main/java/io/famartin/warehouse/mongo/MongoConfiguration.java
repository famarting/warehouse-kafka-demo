package io.famartin.warehouse.mongo;

import java.util.Optional;

import javax.enterprise.inject.Produces;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class MongoConfiguration {
    
    @ConfigProperty(name="mongo.connection-string")
    String mongoConnStr;

    // 
    @ConfigProperty(name="mongo.replicaset")
    Optional<String> mongoReplicaSet;

    // 
    @ConfigProperty(name="mongo.authsource")
    Optional<String> mongoAuthSource;

    @ConfigProperty(name="mongo.database")
    String mongoDatabase;

    @Produces
    public MongoCollection<Document> mongoCollection() {
        String connstr = mongoConnStr;
        if (mongoReplicaSet != null && mongoReplicaSet.isPresent()) {
            connstr = connstr + "&replicaSet=" + mongoReplicaSet.get();
        }
        if (mongoAuthSource != null && mongoAuthSource.isPresent()) {
            connstr = connstr + "&authSource=" + mongoAuthSource.get();
            // connstr = connstr + "&sslinvalidhostnameallowed=true";
        }
        System.out.println("Mongo connection string is : "+connstr);
        MongoClient client = MongoClients.create(connstr);
        return client.getDatabase(mongoDatabase)
            .getCollection("stockrecord");
    }

}