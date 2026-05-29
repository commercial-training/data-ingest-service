package com.kms.dataingestservice.model;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Container(containerName = "records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecord {

    @Id
    @PartitionKey
    private String key;

    private String data;

    @JsonProperty("datetime_added")
    private String datetimeAdded;
}
