package com.google.edgemicro.annotations;

/**
 * Created by seanwilliams on 3/23/17.
 */
public @interface SpikeArrest {
    String timeUnit();
    long allow();
    long bufferSize() default 0;
}
