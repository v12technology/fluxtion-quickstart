package com.fluxtion.learning.quicktart.stoploss;

import com.fluxtion.runtime.annotations.OnEventHandler;
import lombok.Data;

@Data
public class ProfitAndLossTrader {

    private int assetPosition;
    private boolean canHedge = true;

    @OnEventHandler
    public void orderDone(OrderDoneEvent trade){
        System.out.println("-----------------------------------\nHedge order complete");
        canHedge = true;
    }

    public void pnlBreach(double pnl){
        if(canHedge) {
            System.out.println("HEDGE pnl breach " + pnl + " - send a trade to clear position:" + assetPosition);
            canHedge = false;
        }else {
            System.out.println("NO HEDGE pnl breach " + pnl + " - live order still not done");
        }
    }

}
