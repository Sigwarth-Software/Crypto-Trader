package org.cryptotrader.logging.library.repository;

import org.cryptotrader.logging.library.entity.ApplicationException;
import org.cryptotrader.logging.library.entity.LogModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/** JPA repository for application exceptions. */
public interface ApplicationExceptionRepository extends JpaRepository<ApplicationException, Long> {
    List<ApplicationException> findByModuleAndTimestampBetween(
        @NotNull LogModule module,
        @NotNull LocalDateTime fromTime,
        @NotNull LocalDateTime toTime
    );
}
