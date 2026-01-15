package com.FinaSplitter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class MongoConfig {

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    @PostConstruct
    public void setUpAfterConstructor() {
        mappingMongoConverter.setMapKeyDotReplacement("__dot__");
    }
}