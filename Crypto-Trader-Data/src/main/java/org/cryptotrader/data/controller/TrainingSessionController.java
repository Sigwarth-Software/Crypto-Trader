package org.cryptotrader.data.controller;

import org.cryptotrader.data.library.services.TrainingSessionService;
import org.cryptotrader.data.library.communication.request.TrainingSessionRequest;
import org.cryptotrader.api.library.communication.response.OperationSuccessfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/training-session")
public class TrainingSessionController {
    private final TrainingSessionService trainingSessionService;

    @Autowired
    public TrainingSessionController(final TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }


    @RequestMapping("/add")
    public ResponseEntity<OperationSuccessfulResponse> addTrainingSession(@RequestBody final TrainingSessionRequest request) {
        if (request == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        this.trainingSessionService.saveTrainingSession(request);
        return new ResponseEntity<>(new OperationSuccessfulResponse(true), HttpStatus.OK);
    }
}
