package com.google.edgemicro.annotations.processors;

import com.google.edgemicro.process.EdgemicroConfig;
import com.google.edgemicro.annotations.OAuth;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationConfigurationException;

import java.lang.annotation.Annotation;

/**
 * Created by seanwilliams on 3/23/17.
 */
public class OAuthProcessor extends AnnotationProcessor {
    private OAuth annotation;

    public OAuthProcessor(Object bean) {
        this.bean = bean;
        annotation = (OAuth) fetchAnnotation();
        if(annotation == null) throw new AnnotationConfigurationException("Missing @OAuth Annotation.");
        //TODO need to remove the annotation.enabled() feature
        isEnabled = (!annotation.allowNoAuthorization() && annotation.enabled()) ? true : false;
        isPresent = true;
    }

    /**
     * Check that all fields are populated. Does not have to check any annotations because there are default
     * values.
     * //TODO need to add annotation checks anyway. Make sure they are valid values.
     */
    @Override
    public void checkAnnotation() {}

    @Override
    public OAuth getAnnotation(){
        return annotation;
    }

    @Override
    public OAuth getAnnotationCast(){ return (OAuth) annotation; }

    /**
     * Fetch the annotation from the bean.
     *
     * @return the annotation retrieved from the bean as an Annotation.
     */
    private Annotation fetchAnnotation() {
        return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), OAuth.class);
    }

    /**
     * Process the OAuth annotation by updating the org-env-config.yaml file with the values
     * from the annotation.  Assumes config file is present.
     *
     * @returns nothing
     */
    @Override
    public void updateConfigWithAnnotationProperties(EdgemicroConfig config) {
        if(!annotation.enabled()) { //oauth is disabled
            config.update("- oauth", "");
        }
        if(annotation.allowNoAuthorization()){
            config.update("allowNoAuthorization: false", "allowNoAuthorization: true");
        }
    }
}
