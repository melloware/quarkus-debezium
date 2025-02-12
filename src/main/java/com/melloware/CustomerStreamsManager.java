package com.melloware;

import static org.javers.core.diff.ListCompareAlgorithm.LEVENSHTEIN_DISTANCE;

import java.util.Collections;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.serde.DebeziumSerdes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Manager class for handling Customer data streams using Kafka Streams.
 * Processes Debezium CDC events for Customer entities and tracks changes.
 */
@ApplicationScoped
@Slf4j
public class CustomerStreamsManager {

    /** Object mapper for JSON serialization/deserialization */
    @Inject
    ObjectMapper objectMapper;

    /** Configured Kafka topic name for customer events */
    @Inject
    @ConfigProperty(name="customers.topic")
    String customersTopic;

    /**
     * Creates and configures the Kafka Streams topology for processing customer events.
     * The topology handles CREATE, UPDATE, and DELETE operations on Customer records,
     * logging the changes and calculating diffs for updates using Javers.
     *
     * @return A configured Kafka Streams {@link Topology} instance
     */
    @Produces
    Topology createStreamTopology() {
        // Log the start of topology creation
        log.info("Creating stream topology");

        // Create a new StreamsBuilder instance
        StreamsBuilder builder = new StreamsBuilder();

        // Configure the key serde for Long values using Debezium JSON serialization
        Serde<Long> longKeySerde = DebeziumSerdes.payloadJson(Long.class);
        longKeySerde.configure(Collections.emptyMap(), true);

        // Configure the value serde as a String serde for raw JSON
        Serde<String> valueSerde = Serdes.String();
        valueSerde.configure(Collections.emptyMap(), false);

        // Create a stream from the customers topic with configured serdes
        builder.stream(
                        customersTopic,
                        Consumed.with(longKeySerde, valueSerde)
                )
                .peek((k, v) -> {
                    // Skip if value is null
                    if (v == null) return;
                    try {
                        // Parse the JSON value into a DebeziumEvent
                        DebeziumEvent<Customer> event = objectMapper.readValue(v, new TypeReference<>() {});
                        DebeziumEvent.Payload<Customer> payload = event.getPayload();
                        
                        // Handle different operation types
                        switch (payload.getOp()) {
                            case c -> {
                                // Log customer creation
                                log.info("CREATE ID: {} After: {}", k, payload.getAfter());
                            }
                            case u -> {
                                // Log customer update details
                                log.debug("UPDATE ID: {} Before: {}",  k, payload.getBefore());
                                log.debug("UPDATE ID: {} After: {}", k, payload.getAfter());
                                
                                // Create Javers instance for diff calculation
                                Javers javers = JaversBuilder.javers()
                                        .withListCompareAlgorithm(LEVENSHTEIN_DISTANCE)
                                        .build();
                                // Calculate and log the differences between before and after states
                                Diff diff = javers.compare(payload.getBefore(), payload.getAfter());
                                log.info("UPDATE ID: {} Changes: {}", k, javers.getJsonConverter().toJson(diff));
                            }
                            case d -> {
                                // Log customer deletion
                                log.info("DELETE ID: {} Before: {}", k, payload.getBefore());
                            }
                            default -> { throw new IllegalArgumentException("Unknown op: " + payload.getOp()); }
                        }

                    } catch (Exception e) {
                        // Log any JSON parsing errors
                        log.error("Error parsing JSON {}",k, e);
                    }
                });

        // Build and return the topology
        return builder.build();
    }
}