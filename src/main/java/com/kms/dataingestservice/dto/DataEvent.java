package com.kms.dataingestservice.dto;

import java.time.Instant;

public record DataEvent(String key, Instant createdAt, String createdBy) {
}
