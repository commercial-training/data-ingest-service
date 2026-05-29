package com.kms.dataingestservice.config;

import com.azure.messaging.eventhubs.EventProcessorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class EventProcessorLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(EventProcessorLifecycle.class);

    private final EventProcessorClient processor;
    private volatile boolean running;

    public EventProcessorLifecycle(EventProcessorClient processor) {
        this.processor = processor;
    }

    @Override
    public void start() {
        log.info("Starting EventProcessorClient");
        processor.start();
        running = true;
    }

    @Override
    public void stop() {
        log.info("Stopping EventProcessorClient");
        processor.stop();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
