package com.google.edgemicro.annotations;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * Created by seanwilliams on 3/22/17.
 */
@Configuration
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EdgeMicroPrivateConfig {
    String virtualHosts() default "default";
    String runtimeURL();
    String mgmtURL();
}
