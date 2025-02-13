package com.melloware;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

/**
 * Customer entity class representing a CUSTOMER database record.
 */
@Data
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {

    /** The unique identifier for the customer */
    @JsonProperty("ID")
    public int id;
    
    /** The customer's first name */
    @JsonProperty("FIRST_NAME") 
    public String firstName;
    
    /** The customer's last name */
    @JsonProperty("LAST_NAME")
    public String lastName;
    
    /** The customer's email address */
    @JsonProperty("EMAIL")
    public String email;
}