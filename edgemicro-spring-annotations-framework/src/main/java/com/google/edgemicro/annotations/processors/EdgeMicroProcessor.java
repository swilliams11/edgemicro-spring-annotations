package com.google.edgemicro.annotations.processors;

import com.google.edgemicro.annotations.EdgeMicro;
import com.google.edgemicro.annotations.EdgeMicroPrivateConfig;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationConfigurationException;

import java.lang.annotation.Annotation;

/**
 * Created by seanwilliams on 3/23/17.
 *
 * Responsible for processing the @EdgeMicro annotation.
 */
public class EdgeMicroProcessor extends AnnotationProcessor {
    private EdgeMicro annotation;

    public EdgeMicroProcessor(Object bean) {
        this.bean = bean;
        annotation = (EdgeMicro) fetchAnnotation();
        if(annotation == null) throw new AnnotationConfigurationException("Missing @EdgeMicro Annotation.");

        if(annotation.privateConfig()){ //is privateConfig enabled?
            EdgeMicroPrivateConfig emp =  AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), EdgeMicroPrivateConfig.class);
            if(emp == null) throw new AnnotationConfigurationException("Missing @EdgeMicroPrivateConfig Annotation.");
        }
    }

    /**
     * Check that all fields are populated.
     */
    @Override
    public void checkAnnotation() {
        Annotation em = AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), EdgeMicro.class);

        if(annotation.admin().equals("") || annotation.password() == null){
            throw new AnnotationConfigurationException("@EdgeMicro is missing admin property.");
        }

        if(annotation.password().equals("") || annotation.password() == null){
            throw new AnnotationConfigurationException("@EdgeMicro is missing password property.");
        }

        if(annotation.org().equals("") || annotation.org() == null){
            throw new AnnotationConfigurationException("@EdgeMicro is missing org property.");
        }

        if(annotation.env().equals("") || annotation.env() == null){
            throw new AnnotationConfigurationException("@EdgeMicro is missing env property.");
        }
    }

    @Override
    public Annotation getAnnotation(){
        return annotation;
    }

    @Override
    public EdgeMicro getAnnotationCast() { return annotation; }

    /**
     * Fetch the annotation from the bean.
     *
     * @return the annotation retrieved from the bean as an Annotation.
     */
    private Annotation fetchAnnotation() {
        return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), EdgeMicro.class);
    }

    @Override
    public boolean getPropertyBoolean(String key){
        return annotation.privateConfig();
    }
}
