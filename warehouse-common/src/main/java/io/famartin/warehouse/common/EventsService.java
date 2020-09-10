package io.famartin.warehouse.common;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.vertx.core.json.JsonObject;

/**
 * EventsService
 */
@ApplicationScoped
public class EventsService {

    @Inject
    @ConfigProperty(name = "quarkus.application.name", defaultValue = "")
    String appName;

    String podName = System.getenv("POD_NAME");

    public String getServiceName() {
        return Optional.ofNullable(podName)
            .orElseGet(() -> appName + "-" + UUID.randomUUID().toString());
    }

    @Inject
    @Channel("events")
    Emitter<JsonObject> events;

    public void sendEvent(String event) {
        JsonObject msg = new JsonObject();
        msg.put("timestamp", Instant.now().toString());
        msg.put("from", getServiceName());
        msg.put("event", event);
        events.send(msg);
    }

    public void sendStockEvent(String itemId, Integer stock) {
        JsonObject msg = new JsonObject();
        msg.put("timestamp", Instant.now().toString());
        msg.put("from", getServiceName());
        msg.put("type", "stock");
        msg.put("event", new JsonObject().put("itemId", itemId).put("stock", stock));
        events.send(msg);
    }

}