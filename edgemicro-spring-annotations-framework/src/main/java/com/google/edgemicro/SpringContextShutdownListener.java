package com.google.edgemicro;

import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by seanwilliams on 3/22/17.
 */
@Component
public class SpringContextShutdownListener {
    private static Logger logger = Logger.getLogger(SpringContextShutdownListener.class.getName());

    @EventListener
    public void handleContextRefresh(ApplicationContextEvent event) {
        //shutdown the
        logger.info("Shutting down Spring Context...");
    }

}
