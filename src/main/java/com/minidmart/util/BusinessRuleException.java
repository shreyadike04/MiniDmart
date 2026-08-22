package com.minidmart.util;

/** Thrown when a request is well-formed but violates a business rule
 *  (out of stock, slot full, ineligible return, etc). Message is safe to show to the user. */
public class BusinessRuleException extends Exception {
    public BusinessRuleException(String message) {
        super(message);
    }
}
