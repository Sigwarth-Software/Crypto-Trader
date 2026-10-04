package org.cryptotrader.api.config;

//=================================-Imports-==================================
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    //=============================-Methods-==================================

    //------------------------Handle-Any-Exception----------------------------
    @ExceptionHandler(value = {Exception.class})
    public void handleAnyException(@NotNull final Exception exception) {
        log.error("An exception occurred: ", exception);
    }
}
