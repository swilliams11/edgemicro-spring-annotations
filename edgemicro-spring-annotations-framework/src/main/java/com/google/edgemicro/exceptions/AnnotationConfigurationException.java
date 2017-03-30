package com.google.edgemicro.exceptions;

/**
 * Created by seanwilliams on 3/22/17.
 *
 * Runtime exception that is thrown when an annotation is misconfigured or
 * missing.
 */
public class AnnotationConfigurationException extends RuntimeException {
    public AnnotationConfigurationException(String message){
        super(message);
    }
}
