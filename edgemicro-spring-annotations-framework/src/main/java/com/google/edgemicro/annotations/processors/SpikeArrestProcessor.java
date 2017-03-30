package com.google.edgemicro.annotations.processors;

import com.google.edgemicro.process.EdgemicroConfig;
import com.google.edgemicro.annotations.SpikeArrest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationConfigurationException;

import java.lang.annotation.Annotation;
import java.util.logging.Logger;

/**
 * Created by seanwilliams on 3/23/17.
 *
 * Responsible for processing the @SpikeArrest annotation.
 */
public class SpikeArrestProcessor extends AnnotationProcessor {
    private SpikeArrest annotation;
    private static Logger logger = Logger.getLogger(SpikeArrestProcessor.class.getName());

    public SpikeArrestProcessor(Object bean){
        this.bean = bean;
        annotation = (SpikeArrest) fetchAnnotation();
        isEnabled = (annotation != null) ? true : false;
        isPresent = (annotation != null) ? true : false;
    }

    /**
     * Check that all fields are populated.
     */
    @Override
    public void checkAnnotation() {
        if(isPresent) {
            logger.info("@SpikeArrest is present");
            if (annotation.timeUnit().equals("") || annotation.timeUnit() == null) {
                throw new AnnotationConfigurationException("@SpikeArrest is missing timeUnit property.");
            }
            if (!annotation.timeUnit().equals("minute") && !annotation.timeUnit().equals("second")) {
                throw new AnnotationConfigurationException("@SpikeArrest timeUnit property must be either minute or second.");
            }
            if (annotation.allow() == 0) {
                throw new AnnotationConfigurationException("@SpikeArrest allow property cannot be 0.");
            }
        } else {
            logger.info("@SpikeArrest is NOT present");
        }
    }

    @Override
    public Annotation getAnnotation(){
        return annotation;
    }

    @Override
    public SpikeArrest getAnnotationCast(){
        return annotation;
    }

    /**
     * Fetch the annotation from the bean.
     *
     * @return the annotation retrieved from the bean as an Annotation.
     */
    private Annotation fetchAnnotation(){
        return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), SpikeArrest.class);
    }

    /**
     * Process the SpikeArrest annotation by updating the org-env-config.yaml file with the values
     * from the annotation.  Assumes config file is present.
     *
     * @returns nothing
     */
    @Override
    public void updateConfigWithAnnotationProperties(EdgemicroConfig config){
        if(isPresent) {
            logger.info("Updating config file with @SpikeArrest properties");
            long bufferSize = annotation.bufferSize();
            long allow = annotation.allow();
            String timeUnit = annotation.timeUnit();
            StringBuilder sb = new StringBuilder();
            sb.append("\nspikearrest:\n");
            sb.append("  timeUnit: ");
            sb.append(timeUnit);
            sb.append("\n");
            sb.append("  allow: ");
            sb.append(allow);
            sb.append("\n");
            sb.append("  bufferSize: ");
            sb.append(bufferSize);
            sb.append("\n");
            config.append(sb.toString());
        } else {
            logger.info("Config file not updated. @SpikeArrest is not present");
        }
    }
}
