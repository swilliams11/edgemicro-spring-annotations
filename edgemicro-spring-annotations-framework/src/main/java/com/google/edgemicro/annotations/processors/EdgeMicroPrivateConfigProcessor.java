package com.google.edgemicro.annotations.processors;

import com.google.edgemicro.annotations.EdgeMicro;
import com.google.edgemicro.annotations.EdgeMicroPrivateConfig;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationConfigurationException;

import java.lang.annotation.Annotation;

/**
 * Created by seanwilliams on 3/23/17.
 *
 * Responsible for processing the @EdgeMicroPrivateConfig annotation.
 */
public class EdgeMicroPrivateConfigProcessor extends AnnotationProcessor {
    private EdgeMicroPrivateConfig annotation;

    public EdgeMicroPrivateConfigProcessor(Object bean) {
        this.bean = bean;
        annotation = (EdgeMicroPrivateConfig) fetchAnnotation();
        isEnabled = (annotation != null) ? true : false;
    }

    /**
     * Check that all fields are populated.
     */
    @Override
    public void checkAnnotation() {
        EdgeMicro em = AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), EdgeMicro.class);
        if(em == null) {
            throw new AnnotationConfigurationException("@EdgeMicroPrivateConfig annotation is configured, but @EdgeMicro is missing.");
        }

        if(annotation.mgmtURL().equals("") || annotation.mgmtURL() == null){
            throw new AnnotationConfigurationException("@EdgeMicroPrivateConfig is missing mgmtURL property.");
        }

        if(annotation.runtimeURL().equals("") || annotation.runtimeURL() == null){
            throw new AnnotationConfigurationException("@EdgeMicroPrivateConfig is missing runtimeURL property.");
        }
    }

    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public EdgeMicroPrivateConfig getAnnotationCast() {
        return annotation;
    }

    /**
     * Fetch the annotation from the bean.
     *
     * @return the annotation retrieved from the bean as an Annotation.
     */
    private Annotation fetchAnnotation() {
        return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), EdgeMicroPrivateConfig.class);
    }
}
