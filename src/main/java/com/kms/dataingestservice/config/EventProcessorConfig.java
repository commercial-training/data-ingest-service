package com.kms.dataingestservice.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.kms.dataingestservice.service.RecordIngestor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class EventProcessorConfig {

    @Bean
    public CheckpointStore checkpointStore(
            @Value("${spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name}") String accountName,
            @Value("${spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name}") String containerName) {
        BlobContainerAsyncClient blobContainer = new BlobContainerClientBuilder()
                .endpoint("https://" + accountName + ".blob.core.windows.net")
                .containerName(containerName)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
        return new BlobCheckpointStore(blobContainer);
    }

    @Bean(destroyMethod = "stop")
    public EventProcessorClient eventProcessorClient(
            CheckpointStore checkpointStore,
            RecordIngestor ingestor,
            @Value("${spring.cloud.azure.eventhubs.namespace}") String namespace,
            @Value("${spring.cloud.azure.eventhubs.event-hub-name}") String eventHubName,
            @Value("${spring.cloud.azure.eventhubs.processor.consumer-group}") String consumerGroup) {
        Consumer<EventContext> onEvent = ingestor::onEvent;
        Consumer<ErrorContext> onError = ingestor::onError;
        return new EventProcessorClientBuilder()
                .initialPartitionEventPosition(partition  -> EventPosition.latest())
                .credential(namespace + ".servicebus.windows.net", eventHubName, new DefaultAzureCredentialBuilder().build())
                .consumerGroup(consumerGroup)
                .checkpointStore(checkpointStore)
                .processEvent(onEvent)
                .processError(onError)
                .buildEventProcessorClient();
    }
}
