package com.google.edgemicro.annotations.processors;

import com.google.edgemicro.annotations.OAuth;
import com.google.edgemicro.annotations.Quota;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationConfigurationException;

import java.lang.annotation.Annotation;

/**
 * Created by seanwilliams on 3/23/17.
 *
 * Responsible for processing @Quota annotation.
 */
public class QuotaProcessor extends AnnotationProcessor {
    private Quota annotation;

    public QuotaProcessor(Object bean){
        this.bean = bean;
        annotation = (Quota)fetchAnnotation();
        isEnabled = (annotation != null) ? true : false;
    }

    /**
     * OAuth annotation must be configured on bean for this to work correctly.
     */
    @Override
    public void checkAnnotation() {
        if(isOAuthPresent()){
            throw new AnnotationConfigurationException("@OAuth must be enabled for the @Quota to work correctly.");
        }
    }

    /**
     * Determine if the OAuth annotation present.
     *
     * @return true if the OAuth annotation is present, false otherwise
     */
    private boolean isOAuthPresent(){
        return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), OAuth.class) == null;
    }

    @Override
    public Annotation getAnnotation(){
        return annotation;
    }

    @Override
    public Quota getAnnotationCast() {
        return annotation;
    }

    /**
     * Fetch the annotation from the bean.
     *
     * @return the annotation retrieved from the bean as an Annotation.
     */
    private Annotation fetchAnnotation(){
        return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), Quota.class);
    }

}
