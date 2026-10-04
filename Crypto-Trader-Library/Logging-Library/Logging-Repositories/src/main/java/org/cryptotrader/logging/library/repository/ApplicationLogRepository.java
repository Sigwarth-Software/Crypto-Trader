package org.cryptotrader.logging.library.repository;

import org.cryptotrader.logging.library.entity.ApplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, Long> {
    List<ApplicationLog> findByModuleAndTimestampBetween(
            String module, LocalDateTime from, LocalDateTime to);
}
