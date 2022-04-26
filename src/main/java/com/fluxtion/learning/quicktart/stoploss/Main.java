package com.fluxtion.learning.quicktart.stoploss;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.compiler.SEPConfig;
import com.fluxtion.compiler.builder.stream.EventFlow;
import com.fluxtion.runtime.EventProcessor;
import com.fluxtion.runtime.stream.helpers.Mappers;
import com.fluxtion.runtime.stream.helpers.Peekers;

public class Main {

    public static void main(String[] args) {
        EventProcessor tradeController = Fluxtion.interpret(Main::buildPnLControl);
        tradeController.init();
        sendData(tradeController);
    }

    private static void sendData(EventProcessor processor){
        processor.onEvent(new Trade("BTC", 100, 3));
        processor.onEvent(new Trade("NOT-BTC", 100, 35_000));
        processor.onEvent(new PriceUpdate("BTC", 2, 3.0));
        processor.onEvent(new Trade("BTC", 200, 4));
        processor.onEvent(new Trade("BTC", -500, 3));
        processor.onEvent(new PriceUpdate("BTC", 1, 2.0));
    }

    private static void buildPnLControl(SEPConfig cfg){
        ProfitAndLossTrader pnlTrader = new ProfitAndLossTrader();

        var btcTradeStream = EventFlow.subscribe(Trade.class)
                .peek(Peekers.console("-----------------------------------\nexecuted:{}"))
                .filter(Main::filterBTCInstrument);

        var btcPriceStream = EventFlow.subscribe(PriceUpdate.class)
                .peek(Peekers.console("-----------------------------------\nnew price:{}"))
                .filter(Main::filterBTCInstrument)
                .mapToDouble(PriceUpdate::getMidPrice)
                .defaultValue(Double.NaN);

        var cumulativeTradedVolume = btcTradeStream
                .mapToInt(Trade::getVolume)
                .map(new Mappers.SumInt()::add)
                .push(pnlTrader::setAssetPosition)
                .peek(Peekers.console("BTC position:{}"));

        var assetValue = cumulativeTradedVolume
                .mapToDouble(i -> i)
                .map(Mappers.MULTIPLY_DOUBLES, btcPriceStream)
                .peek(Peekers.console("BTC mark to market:{}"));

        btcTradeStream
                .mapToDouble(Trade::getVolume)
                .map(d -> d * -1)
                .map(Mappers.MULTIPLY_DOUBLES, btcTradeStream.mapToDouble(Trade::getPrice))
                .map(new Mappers.SumDouble()::add)
                .peek(Peekers.console("cash position:{}"))
                .map(Mappers.ADD_DOUBLES, assetValue)
                .peek(Peekers.console("BTC trade pnl:{}"))
                .filter(pnl -> pnl < -300 || pnl > 300)
                .push(pnlTrader::pnlBreach);
    }

    public static Boolean filterBTCInstrument(Instrument trade) {
        return trade.getInstrument().equals("BTC");
    }

}
