package com.melloware;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

/**
 * Customer entity class representing a CUSTOMER database record.
 */
@Data
@RegisterForReflection
public class Customer {

    /** The unique identifier for the customer */
    public int ID;
    
    /** The customer's first name */
    public String FIRST_NAME;
    
    /** The customer's last name */
    public String LAST_NAME;
    
    /** The customer's email address */
    public String EMAIL;
}