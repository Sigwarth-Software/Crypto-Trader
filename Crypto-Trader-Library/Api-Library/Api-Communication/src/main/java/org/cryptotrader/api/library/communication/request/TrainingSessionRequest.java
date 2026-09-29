package org.cryptotrader.api.library.communication.request;

import lombok.Data;

@Data
public class TrainingSessionRequest {
    private String currency;
    private Long prediction;
    private int numRows;
    private int epochsTrained;
    private int maxEpochs;
    private double startingLoss;
    private double finalLoss;
    private String modelType;
    private String queryType;
    private String trainingStartTime;
    private String trainingEndTime;
    private String queryStartTime;
    private String queryEndTime;
    private int sequenceLength;
    private int batchSize;
    private int dimensionWidth;
    private String queryLoad;
    private Integer queryBatchSize;
    private String trainingDevice;
    private Integer shortSequenceLength;
    private Integer mediumSequenceLength;
    private Integer longSequenceLength;

    public TrainingSessionRequest(final String currency,
                                  final Long prediction,
                                  final int numRows,
                                  final int epochsTrained,
                                  final int maxEpochs,
                                  final double startingLoss,
                                  final double finalLoss,
                                  final String modelType,
                                  final String queryType,
                                  final String trainingStartTime,
                                  final String trainingEndTime,
                                  final String queryStartTime,
                                  final String queryEndTime,
                                  final int sequenceLength,
                                  final int batchSize,
                                  final int dimensionWidth,
                                  final String queryLoad,
                                  final Integer queryBatchSize,
                                  final String trainingDevice,
                                  final Integer shortSequenceLength,
                                  final Integer mediumSequenceLength,
                                  final Integer longSequenceLength) {
        this.currency = currency;
        this.prediction = prediction;
        this.numRows = numRows;
        this.epochsTrained = epochsTrained;
        this.maxEpochs = maxEpochs;
        this.startingLoss = startingLoss;
        this.finalLoss = finalLoss;
        this.modelType = modelType;
        this.queryType = queryType;
        this.trainingStartTime = trainingStartTime;
        this.trainingEndTime = trainingEndTime;
        this.queryStartTime = queryStartTime;
        this.queryEndTime = queryEndTime;
        this.sequenceLength = sequenceLength;
        this.batchSize = batchSize;
        this.dimensionWidth = dimensionWidth;
        this.queryLoad = queryLoad;
        this.queryBatchSize = queryBatchSize;
        this.trainingDevice = trainingDevice;
        this.shortSequenceLength = shortSequenceLength;
        this.mediumSequenceLength = mediumSequenceLength;
        this.longSequenceLength = longSequenceLength;
    }
}
