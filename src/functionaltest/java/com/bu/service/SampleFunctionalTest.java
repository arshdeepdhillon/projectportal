package com.bu.service;

/*
 * AI Contribution Weight: [97%] AI Generated / [3%] Human Refactored
 */

import com.bu.service.persistenceprobe.controller.PersistenceRecordController.RecordRequest;
import com.bu.service.persistenceprobe.controller.PersistenceRecordController.RecordResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SampleFunctionalTest
{
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void savesAndFetchesRecordThroughHttpAndJpa()
    {
        ResponseEntity<RecordResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/records",
                new RecordRequest( "hello persistence" ),
                RecordResponse.class
        );

        assertThat( createResponse.getStatusCode() ).isEqualTo( HttpStatus.CREATED );
        RecordResponse created = createResponse.getBody();
        assertThat( created ).isNotNull();
        assertThat( created.id() ).isNotNull();
        assertThat( created.value() ).isEqualTo( "hello persistence" );

        ResponseEntity<RecordResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/records/{id}",
                RecordResponse.class,
                created.id()
        );

        assertThat( getResponse.getStatusCode() ).isEqualTo( HttpStatus.OK );
        assertThat( getResponse.getBody() ).isEqualTo( created );
    }
}
