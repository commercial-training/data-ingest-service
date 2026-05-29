package com.kms.dataingestservice.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.kms.dataingestservice.model.ReportRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRecordRepository extends CosmosRepository<ReportRecord, String> {
}
