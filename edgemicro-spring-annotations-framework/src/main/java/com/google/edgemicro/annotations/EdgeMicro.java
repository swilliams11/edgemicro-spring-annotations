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
public @interface EdgeMicro {
    boolean privateConfig() default false;
    String key() default "none";
    String secret() default "none";
    String version() default "latest";
    String org();
    String env();
    String admin();
    //TODO need to remove the password from here and get from ENV variable instead
    String password();
    int port() default 8000;
}
