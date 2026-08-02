package io.github.springwhale.database.flyway.repository;

import io.github.springwhale.database.flyway.entity.FlywayErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FlywayErrorLogRepository extends JpaRepository<FlywayErrorLogEntity, Long>,
        JpaSpecificationExecutor<FlywayErrorLogEntity> {
}