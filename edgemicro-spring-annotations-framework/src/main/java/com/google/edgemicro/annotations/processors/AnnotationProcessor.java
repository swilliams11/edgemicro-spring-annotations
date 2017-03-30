package com.google.edgemicro.annotations.processors;

import com.google.edgemicro.process.EdgemicroConfig;

import java.lang.annotation.Annotation;

/**
 * Created by seanwilliams on 3/23/17.
 *
 * Responsible for sharing Annotation processor code and variables.
 */
public abstract class AnnotationProcessor {
    protected Object bean;
    protected boolean isEnabled;
    //TODO need to implement the isPresent feature - OAuth could be present but not enabled; just because a tag is present doesn't mean that it is enabled.
    protected boolean isPresent;

    public abstract void checkAnnotation();
    public abstract Annotation getAnnotation();
    public abstract <T> T getAnnotationCast();

    /**
     * Subclasses should override this method if the annotations have properties that should be written
     * to the Edge Microgateway configure file.
     *
     * @param config file where the annotation properties should be written.
     */
    public void updateConfigWithAnnotationProperties(EdgemicroConfig config){
        throw new UnsupportedOperationException("This operation is not required for this annotation.");
    };

    //default implementations for annotations.
    public boolean isEnabled() {
        return isEnabled;
    }
    public String getPropertyString(String key){
        return null;
    }
    public boolean getPropertyBoolean(String key){
        return false;
    }
    public int getPropertyInt(String key){
        return 0;
    }
}
