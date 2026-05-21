package org.cryptotrader.data.library.services;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.data.library.communication.request.TrainingSessionRequest;
import org.cryptotrader.data.library.entity.training.TrainingSession;
import org.cryptotrader.data.library.entity.training.builder.TrainingSessionBuilder;
import org.cryptotrader.data.library.repository.TrainingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionBuilder trainingSessionBuilder;

    @Autowired
    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository,
                                  TrainingSessionBuilder trainingSessionBuilder) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingSessionBuilder = trainingSessionBuilder;
    }

    @TimeTracked(expectedMillis = 500, shouldPersist = true)
    @Transactional
    public void saveTrainingSession(TrainingSessionRequest request) {
        TrainingSession trainingSession = this.fromRequest(request);
        this.trainingSessionRepository.save(trainingSession);
    }

    public TrainingSession fromRequest(TrainingSessionRequest request) {
        return this.trainingSessionBuilder
                .currency(request.getCurrency())
                .prediction(request.getPrediction())
                .numRows(request.getNumRows())
                .epochsTrained(request.getEpochsTrained())
                .maxEpochs(request.getMaxEpochs())
                .startingLoss(request.getStartingLoss())
                .finalLoss(request.getFinalLoss())
                .modelType(request.getModelType())
                .queryType(request.getQueryType())
                .trainingStartTime(request.getTrainingStartTime())
                .trainingEndTime(request.getTrainingEndTime())
                .queryStartTime(request.getQueryStartTime())
                .queryEndTime(request.getQueryEndTime())
                .sequenceLength(request.getSequenceLength())
                .batchSize(request.getBatchSize())
                .dimensionWidth(request.getDimensionWidth())
                .queryLoad(request.getQueryLoad())
                .queryBatchSize(request.getQueryBatchSize())
                .trainingDevice(request.getTrainingDevice())
                .shortSequenceLength(request.getShortSequenceLength())
                .mediumSequenceLength(request.getMediumSequenceLength())
                .longSequenceLength(request.getLongSequenceLength())
                .build();
    }
}
