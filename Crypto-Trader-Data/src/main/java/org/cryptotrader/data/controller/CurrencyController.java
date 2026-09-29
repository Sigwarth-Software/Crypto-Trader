package org.cryptotrader.data.controller;

import org.cryptotrader.data.library.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("!docs")
@RequestMapping("/data/currency")
public class CurrencyController {
    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }
}
