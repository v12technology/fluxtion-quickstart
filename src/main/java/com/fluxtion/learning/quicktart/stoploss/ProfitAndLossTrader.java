package com.fluxtion.learning.quicktart.stoploss;

import com.fluxtion.runtime.annotations.OnEventHandler;
import com.fluxtion.runtime.annotations.OnTrigger;
import lombok.Data;

@Data
public class ProfitAndLossTrader {

    private int assetPosition;

    @OnEventHandler
    public void processTrade(Trade trade){
        System.out.println("processing trade ...." + trade);
    }

    public void pnlBreach(double pnl){
        System.out.println("pnl breach - send a trade to clear position:" + assetPosition);
    }

    @OnTrigger
    public void checkPositionHedgeRequired(){
        System.out.println("checking if hedge trade required ....");
    }
}
