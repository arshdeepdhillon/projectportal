package com.bu;

/*
 * AI Contribution Weight: [96%] AI Generated / [4%] Human Refactored
 */

import com.bu.model.PersistenceRecord;
import com.bu.repository.PersistenceRecordRepository;
import com.bu.service.persistenceprobe.featureservice.PersistenceRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleUnitTest
{
    @Mock
    private PersistenceRecordRepository repository;

    @InjectMocks
    private PersistenceRecordService service;

    @Test
    void savesRecordValue()
    {
        when( repository.save( any( PersistenceRecord.class ) ) )
                .thenAnswer( invocation -> invocation.getArgument( 0 ) );

        PersistenceRecord saved = service.save( "hello persistence" );

        assertThat( saved.getValue() ).isEqualTo( "hello persistence" );
    }
}
