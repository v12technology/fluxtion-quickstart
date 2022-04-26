package com.fluxtion.learning.quicktart.stoploss;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class Trade implements Instrument {
    String instrument;
    int volume;
    double price;
}
