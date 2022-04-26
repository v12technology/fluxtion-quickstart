package com.fluxtion.learning.quicktart.stoploss;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class TradeEvent implements InstrumentEvent {
    String instrument;
    int volume;
    double price;
}
