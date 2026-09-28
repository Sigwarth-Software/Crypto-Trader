package org.cryptotrader.data.controller;

import org.cryptotrader.data.library.services.PricePredictionService;
import org.cryptotrader.data.library.communication.request.PricePredictionRequest;
import org.cryptotrader.data.library.communication.response.PredictionIdResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/predictions")
public class PricePredictionController {
    private final PricePredictionService pricePredictionService;

    @Autowired
    public PricePredictionController(final PricePredictionService pricePredictionService) {
        this.pricePredictionService = pricePredictionService;
    }
    @RequestMapping("/add")
    public @NotNull ResponseEntity<PredictionIdResponse> predictions(@RequestBody final @Nullable PricePredictionRequest pricePredictionRequest) {
        if (pricePredictionRequest == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final Long predictionId = this.pricePredictionService.savePrediction(pricePredictionRequest).getId();
        if (predictionId == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(new PredictionIdResponse(predictionId), HttpStatus.ACCEPTED);
    }
}
