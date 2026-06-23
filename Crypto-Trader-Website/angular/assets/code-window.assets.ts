import { CodeWindow } from '@models/promo/types';

export const analysisCodeSnippet: string = `model = Sequential([
    LSTM(128, return_sequences=True,
         input_shape=(window, features)),
    Dropout(0.3),
    LSTM(64, return_sequences=False),
    Dense(32, activation='relu'),
    Dense(1, activation='linear')
])`;

export const analysisCodeTitle: string = 'analysis — LSTM Model Architecture';

export const analysisCodeWindow: CodeWindow = {
    code: analysisCodeSnippet,
    title: analysisCodeTitle,
};

const homeEngineCodeTitle = 'TradeEngine.kt';

export const homeEngineCodeSnippet = `class TradeEngine {
    fun execute(strategy: Strategy) {
        val signal = strategy.analyze(market)
        if (signal.confidence > threshold) {
            broker.place(signal.order)
            monitor.log(signal)
        }
    }
}`;

export const homeEngineCodeWindow: CodeWindow = {
    code: homeEngineCodeSnippet,
    title: homeEngineCodeTitle,
}