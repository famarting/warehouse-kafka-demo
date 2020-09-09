package io.famartin.warehouse.common;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OrderDeserializer extends JsonbDeserializer<OrderRecord> {

    public OrderDeserializer() {
        super(OrderRecord.class);
    }
}