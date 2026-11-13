package com.bu.repository;

/*
 * AI Contribution Weight: [100%] AI Generated / [0%] Human Refactored
 */

import com.bu.model.PersistenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersistenceRecordRepository extends JpaRepository<PersistenceRecord, UUID>
{
}
