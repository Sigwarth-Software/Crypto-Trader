package org.cryptotrader.api.controller;

import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.communication.request.FuzzyTimeValueRequest;
import org.cryptotrader.api.library.communication.response.*;
import org.cryptotrader.universal.library.communication.response.TimeValueResponse;
import org.cryptotrader.data.library.model.currency.PerformanceRating;
import org.cryptotrader.data.library.services.CurrencyService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.cryptotrader.api.library.communication.request.AssetValueRequest;
import org.cryptotrader.data.library.entity.currency.Currency;

import java.util.List;

@Slf4j
@RestController
@Profile("!docs")
@RequestMapping("/api/currency")
public class CurrencyController {
    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(@NotNull final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PermitAll
    @PostMapping("/value")
    public ResponseEntity<AssetValueResponse> getCurrencyValue(
        @NotNull @RequestBody final AssetValueRequest assetValueRequest
    ) {
        final Currency currency =
            this.currencyService.getCurrencyByCurrencyCode(
                assetValueRequest.getCurrencyCode()
            );

        if (currency == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final double assetValue = currency.getValue() * assetValueRequest.getShares();
            return new ResponseEntity<>(new AssetValueResponse(assetValue), HttpStatus.OK);
        }
    }
    @PermitAll
    @GetMapping("/performance/{currencyCode}")
    public ResponseEntity<PerformanceRatingResponse> getCurrencyPerformance(
        @Nullable @PathVariable final String currencyCode
    ) {
        if (currencyCode == null || currencyCode.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            final PerformanceRating performanceRatingResponse =
                this.currencyService.getDayPerformance(currencyCode);

            if (performanceRatingResponse == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            final String changePercent =
                this.currencyService.getPercentageDayPerformance(
                    currencyCode
                );

            if (changePercent == null || changePercent.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(new PerformanceRatingResponse(performanceRatingResponse, changePercent),
                                        HttpStatus.OK);
        }
    }

    @PermitAll
    @GetMapping("/list")
    public ResponseEntity<CurrencyNamesResponse> getList(
        @RequestParam(value = "withCode", defaultValue = "false") final boolean withCode
    ) {
        final CurrencyNamesResponse response = new CurrencyNamesResponse(
            this.currencyService.getCurrencyNames(withCode)
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PermitAll
    @GetMapping("/all")
    public ResponseEntity<DisplayCurrencyListResponse> getAll() {
        return new ResponseEntity<>(this.currencyService.getCurrencyValuesResponse(), HttpStatus.OK);
    }

    @PermitAll
    @GetMapping(value = "/all", params = "offset")
    public ResponseEntity<DisplayCurrencyListResponse> getAllWithOffset(
        @RequestParam(value = "offset", defaultValue = "0") final int offset
    ) {
        if (offset < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
            this.currencyService.getCurrencyValuesResponse(offset),
            HttpStatus.OK
        );
    }

    @PermitAll
    @GetMapping("/display/{code}")
    public ResponseEntity<DisplayCurrencyResponse> getDisplayCurrency(
        @PathVariable final String code
    ) {
        if (code == null || code.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final Currency currency =
            this.currencyService.getCurrencyByCurrencyCode(code);

        if (currency == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final DisplayCurrencyResponse response = this.currencyService.toCurrencyValueResponse(currency);

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PermitAll
    @GetMapping("/history/{code}")
    public ResponseEntity<List<TimeValueResponse>> getHistory(
        @Nullable @PathVariable final String code,
        @RequestParam(value = "hours", defaultValue = "24") final int hours,
        @RequestParam(value = "intervalSeconds", defaultValue = "60") final int intervalSeconds
    ) {
        if (code == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final List<TimeValueResponse> history =
            this.currencyService.getCurrencyHistory(
                code,
                hours,
                intervalSeconds
            );

        if (history == null || history.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(history, HttpStatus.OK);
    }
    @PermitAll
    @PostMapping("/history/fuzzy/{code}")
    public ResponseEntity<TimeValueResponse> getFuzzyHistory(
        @Nullable @PathVariable final String code,
        @NotNull @RequestBody final FuzzyTimeValueRequest request
    ) {
        if (code == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final TimeValueResponse response =
            this.currencyService.getFuzzyCurrencyHistory(code, request);

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
