package org.cryptotrader.logging.library.repository;

import org.cryptotrader.logging.library.entity.ExecutionSpeedLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionSpeedLogRepository extends JpaRepository<ExecutionSpeedLog, Long> {

}
