package org.cryptotrader.logging.config;
//=================================-Imports-==================================
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@ConditionalOnProperty(prefix = "cryptotrader.exceptions", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GlobalExceptionHandler {
    //=============================-Methods-==================================

    //------------------------Handle-Any-Exception----------------------------
    @ExceptionHandler(value = {Exception.class})
    public void handleAnyException(final Exception exception) {
        log.error("An exception occurred: ", exception);
    }
}
