import { type TradeEvent } from '@models/trader/types';

export const defaultTradeEvent: TradeEvent = {
    id: 0,
    currency: '',
    sharesChange: 0,
    tradeTime: '',
    tradeType: '',
    valueChange: 0,
    vendor: '',
};
