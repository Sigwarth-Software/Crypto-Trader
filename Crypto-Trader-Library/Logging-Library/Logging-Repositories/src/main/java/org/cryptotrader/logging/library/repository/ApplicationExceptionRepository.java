package org.cryptotrader.logging.library.repository;

import org.cryptotrader.logging.library.entity.ApplicationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationExceptionRepository extends JpaRepository<ApplicationException, Long> {
    List<ApplicationException> findByModuleAndTimestampBetween(
            String module, LocalDateTime from, LocalDateTime to);
}
