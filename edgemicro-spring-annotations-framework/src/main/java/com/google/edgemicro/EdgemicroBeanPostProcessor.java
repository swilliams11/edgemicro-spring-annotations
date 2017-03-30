package com.google.edgemicro;

import com.google.edgemicro.annotations.processors.*;
import com.google.edgemicro.process.EdgeMicroProcess;
import com.google.edgemicro.process.EdgemicroConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created by seanwilliams on 3/20/17.
 * This is a special bean that is loaded by the Spring container before the other
 * beans and scans all the Spring beans to determine if they have any Edge
 * Microgateway annotations.
 * If so, then it starts Edge Microgateway.
 */
@Component
public class EdgemicroBeanPostProcessor implements BeanPostProcessor, Ordered {
    private static Logger logger = Logger.getLogger(EdgemicroBeanPostProcessor.class.getName());
    private int order;
    private ConfigurableListableBeanFactory configurableBeanFactory;
    private Map<String, Annotation> annotationList = new HashMap<>();

    //TODO these should be converted to shorts instead to conserve on space
    public static final String EDGEMICRO = "edgemicro";
    public static final String PRIVATE = "private";
    public static final String OAUTH = "oauth";
    public static final String EDGEMICRO_CONFIG_FILE = "edgemicroConfigFile";

    private AnnotationProcessor quotaProcessor;
    private AnnotationProcessor spikeArrestProcessor;
    private AnnotationProcessor edgemicroProcessor;
    private AnnotationProcessor edgemicroPrivateConfigProcessor;
    private AnnotationProcessor oauthProcessor;
    private EdgemicroConfig edgemicroConfig;


    @Autowired
    public EdgemicroBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
        this.configurableBeanFactory = beanFactory;
    }

    //public EdgemicroBeanPostProcessor() {
    //    logger.info("Created InstanceValidationBeanPostProcessor instance");
    //}

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        //logger.info("postProcessBeforeInitialization method invoked");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        //logger.info("postProcessAfterInitialization method invoked");

        this.scanForAnnotations(bean, beanName);
        return bean;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Scan the bean for Edgemicro Annotations. If the bean has the @EdgeMicro
     * annotation then we need to setup Edge Microgateway.
     *
     * @param bean to scan
     * @param beanName bean name as a String
     */
    protected void scanForAnnotations(Object bean, String beanName) {
        //Annotation em = AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), EdgeMicro.class);
        //if(em != null){ //production
        if(beanName.equals("greetingController")){//this is for testing only
            logger.info(beanName);
            boolean temp = true;
            this.setupEdgeMicrogateway(bean);
        }
    }

    /**
     * Setup Microgateway only if it passes the annotation checks.
     *
     * @param bean the bean which has the annotations.
     */
    private void setupEdgeMicrogateway(Object bean) {
        if(hasRequiredAnnotations(bean)){
            setup(bean);
        }
    }

    /**
     * Checks the annotations on a bean.
     *
     * @param bean the bean to check
     * @return true if the bean contains all of the required annotations.
     * @throws com.google.edgemicro.exceptions.AnnotationConfigurationException if an annotation is missing or invalid
     */
    //TODO extract these checks into a separate class.
    private boolean hasRequiredAnnotations(Object bean) {
        edgemicroProcessor = new EdgeMicroProcessor(bean);
        edgemicroProcessor.checkAnnotation();

        oauthProcessor = new OAuthProcessor(bean);
        oauthProcessor.checkAnnotation();

        quotaProcessor = new QuotaProcessor(bean);
        quotaProcessor.checkAnnotation();
        spikeArrestProcessor = new SpikeArrestProcessor(bean);
        spikeArrestProcessor.checkAnnotation();

        edgemicroPrivateConfigProcessor = new EdgeMicroPrivateConfigProcessor(bean);
        edgemicroPrivateConfigProcessor.checkAnnotation();

        if(edgemicroPrivateConfigProcessor.isEnabled()){
            annotationList.put(PRIVATE, edgemicroPrivateConfigProcessor.getAnnotation());
        }

        annotationList.put(EDGEMICRO, edgemicroProcessor.getAnnotation());
        annotationList.put(OAUTH, oauthProcessor.getAnnotation());
        return true;
    }

    /**
     * orchestrates the setup of Edge Microgateway and processing the annotations.
     *
     * @param bean
     */
    private void setup(Object bean){
        EdgeMicroProcess emp = new EdgeMicroProcess(annotationList);
        emp.registerShutdownHook();
        emp.configure();
        //retrieve the file and store content
        edgemicroConfig = new EdgemicroConfig(emp.getProperty(EDGEMICRO_CONFIG_FILE));
        oauthProcessor.updateConfigWithAnnotationProperties(edgemicroConfig);
        edgemicroConfig.save();
        emp.start();
    }
}
