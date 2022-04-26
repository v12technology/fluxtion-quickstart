package com.fluxtion.learning.quicktart.stoploss;

import lombok.Value;

@Value
public class PriceUpdateEvent implements InstrumentEvent {
    String instrument;
    double bid;
    double offer;

    public double getMidPrice() {
        return (bid + offer)/2.0;
    }
}
