package io.famartin.warehouse;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.famartin.warehouse.common.EventsService;
import io.famartin.warehouse.common.OrderRecord;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.smallrye.mutiny.Uni;

/**
 * OrdersService
 */
@ApplicationScoped
public class OrdersService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Random random = new Random();

    @Inject
    EventsService events;

    @Inject
    StocksService stocks;

    @Inject
    @Channel("processed-orders")
    Emitter<OrderRecord>  processedOrders;

    Counter successfulOrdersCounter;
    Counter failedOrdersCounter;

    @Inject
    private MeterRegistry registry;
 
    @PostConstruct
    public void createCounters() { 
        successfulOrdersCounter = Counter.builder("warehouse.orders.success").register(registry);
        failedOrdersCounter = Counter.builder("warehouse.orders.failed").register(registry);
    }

    @Incoming("orders")
    public Uni<Void> processOrder(OrderRecord order) {
        events.sendEvent("Processing order "+order.getOrderId());
        return stocks.requestStock(order.getOrderId(), order.getItemId(), order.getQuantity())
            .map(result -> {
                longRunningOperation();
                order.setProcessingTimestamp(Instant.now().toString());
                order.setProcessedBy(events.getServiceName());
                if (result.getError() != null) {
                    order.setError(result.getError());
                    order.setApproved(false);
                } else if (result.getApproved()) {
                    order.setApproved(true);
                } else {
                    order.setApproved(false);
                    order.setReason(result.getMessage());
                }
                processedOrders.send(order);
                if (order.getApproved()) {
                    successfulOrdersCounter.increment();
                } else {
                    failedOrdersCounter.increment();
                }
                events.sendEvent("Order " + order.getOrderId() + " processed");
                return null;
            });
    }

    private void longRunningOperation() {
        try {
            Thread.sleep(random.nextInt(3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}