package com.kms.dataingestservice;

import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCosmosRepositories(basePackages = "com.kms.dataingestservice.repository")
public class DataIngestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataIngestServiceApplication.class, args);
    }

}
