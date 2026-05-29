package com.kms.dataingestservice.service;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.dataingestservice.dto.DataEvent;
import com.kms.dataingestservice.model.ReportRecord;
import com.kms.dataingestservice.repository.ReportRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RecordIngestor {

    private static final Logger log = LoggerFactory.getLogger(RecordIngestor.class);

    private final ReportRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final String blobBaseUrl;

    public RecordIngestor(ReportRecordRepository repository,
                          ObjectMapper objectMapper,
                          @Value("${spring.cloud.azure.storage.blob.endpoint}") String storageEndpoint,
                          @Value("${spring.cloud.azure.storage.blob.container-name}") String storageContainer) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.blobBaseUrl = stripTrailingSlash(storageEndpoint) + "/" + storageContainer;
    }

    public void onEvent(EventContext context) {
        byte[] body = context.getEventData().getBody();
        DataEvent event;
        try {
            event = objectMapper.readValue(body, DataEvent.class);
        } catch (Exception ex) {
            log.error("Skipping malformed event at partition={} offset={}",
                    context.getPartitionContext().getPartitionId(),
                    context.getEventData().getOffsetString(), ex);
            context.updateCheckpoint();
            return;
        }

        ReportRecord record = new ReportRecord(
                event.key(),
                blobBaseUrl + "/" + event.key(),
                Instant.now().toString()
        );

        try {
            repository.save(record);
            log.info("Ingested record key={} partition={} offset={}",
                    event.key(),
                    context.getPartitionContext().getPartitionId(),
                    context.getEventData().getOffsetString());
        } catch (Exception ex) {
            log.error("Failed to upsert record key={}", event.key(), ex);
            throw ex;
        }

        context.updateCheckpoint();
    }

    public void onError(ErrorContext context) {
        log.error("EventProcessor error on partition={} operation={}",
                context.getPartitionContext().getPartitionId(),
                context.getThrowable().getMessage(),
                context.getThrowable());
    }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
