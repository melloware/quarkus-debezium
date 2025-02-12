package com.melloware;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.Getter;

/**
 * Class representing a Debezium Change Data Capture (CDC) event.
 * This class captures database changes in a structured format.
 *
 * @param <T> The type of the entity being tracked for changes
 */
@Data
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumEvent<T> {
    /** The payload containing the change event details */
    private Payload<T> payload;

    /**
     * Inner class representing the payload of a Debezium event.
     * Contains information about the change operation and affected data.
     *
     * @param <T> The type of the entity being tracked
     */
    @Data
    public static class Payload<T> {
        /** The type of operation performed (create, update, delete) */
        private Operation op;
        /** The state of the record before the change */
        private T before;
        /** The state of the record after the change */
        private T after;
        /** Metadata about the source of the change */
        private Source source;
    }

    /**
     * Inner class containing metadata about the source of the change event.
     */
    @Data
    public static class Source {
        /** The name of the database table */
        private String table;
        /** The database schema name */
        private String schema;
        /** The version of the source connector */
        private String version;
        /** The name of the connector that generated this event */
        private String connector;
        /** The database user who initiated the change */
        private String user_name;
    }

    /**
     * Enum representing the types of database operations that can occur.
     */
    @Getter
    public enum Operation {
        /** Create operation */
        c("CREATE"),
        /** Update operation */
        u("UPDATE"),
        /** Delete operation */
        d("DELETE"),
        /** Unknown operation */
        UNKNOWN(""); // Default fallback

        /** The text representation of the operation */
        private final String text;

        /**
         * Constructor for Operation enum.
         *
         * @param text The text representation of the operation
         */
        Operation(String text) {
            this.text = text;
        }

        /**
         * Converts a text representation to its corresponding Operation enum value.
         *
         * @param text The text to convert
         * @return The matching Operation enum value, or UNKNOWN if no match is found
         */
        public static Operation fromText(String text) {
            for (Operation op : values()) {
                if (op.text.equals(text)) {
                    return op;
                }
            }
            return UNKNOWN; // Fallback for unknown values
        }
    }

}