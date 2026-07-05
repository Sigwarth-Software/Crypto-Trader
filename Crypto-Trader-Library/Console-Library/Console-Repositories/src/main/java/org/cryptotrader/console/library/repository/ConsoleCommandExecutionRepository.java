package org.cryptotrader.console.library.repository;

import org.cryptotrader.console.library.entity.ConsoleCommandExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsoleCommandExecutionRepository extends JpaRepository<ConsoleCommandExecution, Long> {

}
