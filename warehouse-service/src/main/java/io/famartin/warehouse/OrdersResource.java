package io.famartin.warehouse;

import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.famartin.warehouse.common.EventsService;
import io.famartin.warehouse.common.OrderRecord;
import io.vertx.core.json.JsonObject;

@Path("/orders")
public class OrdersResource {

    @Inject
    @Channel("orders")
    Emitter<OrderRecord> orders;

    @Inject
    EventsService events;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response request(@NotNull OrderRecord order) {
        order.setOrderId(UUID.randomUUID().toString());
        if(isValid(order)) {
            orders.send(order);
            events.sendEvent(String.format("Order %s enqueued", order.getOrderId()));
            return Response.ok(order).build();
        } else {
            events.sendEvent(String.format("Invalid order %s", order.toString()));
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    private boolean isValid(OrderRecord order) {
        try {
            return order != null && order.getOrderId()!=null
            && order.getItemId()!=null && order.getQuantity()!=null && order.getQuantity()>0;
        } catch (RuntimeException e) {
            return false;
        }
    }

}