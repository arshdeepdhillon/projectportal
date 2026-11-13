package com.bu.service.persistenceprobe.featureservice;

/*
 * AI Contribution Weight: [100%] AI Generated / [0%] Human Refactored
 */

import com.bu.model.PersistenceRecord;
import com.bu.repository.PersistenceRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PersistenceRecordService {
    //private final String m_sonarTestWarning = "unused field for Sonar rule test";
    private final PersistenceRecordRepository repository;

    public PersistenceRecordService(PersistenceRecordRepository repository) {
        this.repository = repository;
    }

    public PersistenceRecord save(String value) {
        return repository.save(new PersistenceRecord(value));
    }

    @Transactional(readOnly = true)
    public PersistenceRecord getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
    }

    public List<PersistenceRecord> getAll()
    {
        return repository.findAll();
    }
}
