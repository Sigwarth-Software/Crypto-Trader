package org.cryptotrader.data.controller;

import org.cryptotrader.data.library.services.NewsSentimentService;
import org.cryptotrader.data.library.communication.request.NewsSentimentRequest;
import org.cryptotrader.api.library.communication.response.OperationSuccessfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/news-sentiment")
public class NewsSentimentController {
    private final NewsSentimentService newsSentimentService;

    @Autowired
    public NewsSentimentController(final NewsSentimentService newsSentimentService) {
        this.newsSentimentService = newsSentimentService;
    }

    @PostMapping("/add")
    public ResponseEntity<OperationSuccessfulResponse> add(@RequestBody final NewsSentimentRequest request) {
        if (request == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        this.newsSentimentService.saveFromRequest(request);
        return new ResponseEntity<>(new OperationSuccessfulResponse(true), HttpStatus.OK);
    }
}
