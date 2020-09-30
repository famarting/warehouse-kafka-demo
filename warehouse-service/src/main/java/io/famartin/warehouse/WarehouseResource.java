package io.famartin.warehouse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.reactivestreams.Publisher;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.famartin.warehouse.common.EventsService;
import io.famartin.warehouse.common.OrderRecord;
import io.famartin.warehouse.common.StockRecord;
import io.quarkus.runtime.StartupEvent;
import io.reactivex.Flowable;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@Path("/warehouse")
public class WarehouseResource {

    @Inject
    @Channel("processed-orders")
    Multi<OrderRecord> orders;

    @Inject
    @Channel("events-sink")
    Multi<JsonObject> events;

    @Inject
    StocksService stocks;

    private Jsonb json = JsonbBuilder.create();

    Multi<String> getPingStream() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(10))
                .onItem().transform(x -> "{}");
    }

    private class EventsRecordingStatus {
        List<WarehouseEventRecord> recordedEvents;
        Cancellable recordingProcess;

        void startRecordingEvents() {
            recordedEvents = new ArrayList<>();
            recordingProcess = Multi.createBy().merging()
                .streams(eventsToRecord(), ordersToRecord())
                .subscribe()
                .with(e -> recordedEvents.add(e));
        }

        void stopRecording() {
            recordingProcess.cancel();
            recordingProcess = null;
        }
    }

    // private List<String> recordedEvents;
    // private Cancellable recordingProcess;

    private Map<String,EventsRecordingStatus> statuses = new ConcurrentHashMap<>(); 

    @GET
    @Path("/status/{subid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(@PathParam("subid") String subscriptionId) {
        if (subscriptionId == null) {
            subscriptionId = UUID.randomUUID().toString();
        }
        EventsRecordingStatus status = statuses.computeIfAbsent(subscriptionId, k -> new EventsRecordingStatus());

        if (status.recordedEvents != null) {
            //stop
            status.stopRecording();
            //gather cache
            List<WarehouseEventRecord> cache = status.recordedEvents;
            //trigger new recording process
            status.startRecordingEvents();
            //return cached events
            return Response.ok().entity(cache).header("subid", subscriptionId).build();
        } else {
            //trigger recording process
            status.startRecordingEvents();
            return Response.ok().entity(new ArrayList<>()).header("subid", subscriptionId).build();
        }
    }

    public Multi<WarehouseEventRecord> eventsToRecord() {
        return events
            .filter(e -> e.getString("type", "").equals("stock"))
            .map(e -> {
                WarehouseEventRecord r = new WarehouseEventRecord();
                r.setEventId(UUID.randomUUID().toString());
                r.setEventType("STOCK");
                r.setItemId(e.getJsonObject("event").getString("itemId"));
                r.setQuantity(e.getJsonObject("event").getInteger("stock"));
                r.setProcessedBy(e.getString("from"));
                r.setTimestamp(e.getString("timestamp"));
                r.setMessage("Stock updated");
                return r;
            });
    }

    public Multi<WarehouseEventRecord> ordersToRecord() {
        return orders
            .map(o -> {
                WarehouseEventRecord r = new WarehouseEventRecord();
                r.setEventId(o.getOrderId());
                r.setEventType("ORDER");
                r.setItemId(o.getItemId());
                r.setQuantity(o.getQuantity());
                r.setProcessedBy(o.getProcessedBy());
                r.setTimestamp(o.getProcessingTimestamp());
                r.setMessage(o.getApproved() ? "Approved" : "Rejected. "+o.getReason());
                return r;
            });
    }

    @GET
    @Path("/ssestatus")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> all() {
        return Multi.createBy().merging()
        .streams(
            events
                .filter(e -> e.getString("type", "").equals("stock"))
                .map(e -> {
                    WarehouseEventRecord r = new WarehouseEventRecord();
                    r.setEventId(UUID.randomUUID().toString());
                    r.setEventType("STOCK");
                    r.setItemId(e.getJsonObject("event").getString("itemId"));
                    r.setQuantity(e.getJsonObject("event").getInteger("stock"));
                    r.setProcessedBy(e.getString("from"));
                    r.setTimestamp(e.getString("timestamp"));
                    r.setMessage("Stock updated");
                    return r;
                })
                .map(json::toJson),
            orders
                .map(o -> {
                    WarehouseEventRecord r = new WarehouseEventRecord();
                    r.setEventId(o.getOrderId());
                    r.setEventType("ORDER");
                    r.setItemId(o.getItemId());
                    r.setQuantity(o.getQuantity());
                    r.setProcessedBy(o.getProcessedBy());
                    r.setTimestamp(o.getProcessingTimestamp());
                    r.setMessage(o.getApproved() ? "Approved" : "Rejected. "+o.getReason());
                    return r;
                })
                .map(json::toJson),
            getPingStream()
        );
    }

    @GET
    @Path("/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> events() {
        return Multi.createBy().merging()
        .streams(
            events.map(b -> b.encode()),
            getPingStream()
        );
        // return Flowable.fromPublisher(events).map(JsonObject::encode);
    }

    @GET
    @Path("/orders")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> orders() {
        return Multi.createBy().merging()
        .streams(
            orders.map(b -> json.toJson(b)),
            getPingStream()
        );
        // return Flowable.fromPublisher(orders).map(JsonObject::encode);
    }

    @GET
    @Path("/stocks")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StockRecord> currentStocks() {
        return stocks.status();
    }

    @POST
    @Path("/stocks")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<StockRecord> addStock(StockRecord request) {
        return stocks.addStock(UUID.randomUUID().toString(), request.getItemId(), request.getQuantity());
    }

    @Inject
    KubernetesClient kubernetesClient;
    @Inject
    EventsService eventsService;

    @GET
    @Path("/cloudmeta")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getCloudMetadata() {
        // String nodeName = System.getenv("CURRENT_NODE_NAME");
        Node node = kubernetesClient.nodes().list().getItems().get(0);
        String providerId = node.getSpec().getProviderID();
        if (providerId != null) {
            providerId = providerId.split(":")[0];
            String zone = node.getMetadata().getLabels().get("failure-domain.beta.kubernetes.io/zone");
            if (zone != null) {
                zone = zone.toLowerCase();
            }
            return new JsonObject().put("cloud", providerId).put("zone", zone).put("pod", eventsService.getServiceName());
        } else {
            String zone = node.getMetadata().getAnnotations().get("warehouse.zone");
            return new JsonObject().put("cloud", "Baremetal").put("zone", zone).put("pod", eventsService.getServiceName());
        }
    }
}
