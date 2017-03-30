package com.google.edgemicro.annotations;

import org.springframework.context.annotation.Configuration;
import java.lang.annotation.*;

/**
 * Created by seanwilliams on 3/20/17.
 */
@Configuration
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OAuth {
    //TODO - need to remove this enabled featured, it is duplicative of allowNoAuthorization
    boolean enabled() default true;
    boolean allowNoAuthorization() default false;
}


