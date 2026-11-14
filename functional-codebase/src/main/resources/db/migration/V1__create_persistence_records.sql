-- A2 functional baseline: Java 17, Spring Boot 3.3.5, PostgreSQL/H2, Gradle 9.6.0.
-- Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored.
-- Human verification: Done

create table persistence_records (
    id UUID primary key,
    record_value varchar(255) not null
);
