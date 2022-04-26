package com.fluxtion.learning.quicktart.stoploss;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.compiler.SEPConfig;
import com.fluxtion.compiler.builder.stream.EventFlow;
import com.fluxtion.runtime.EventProcessor;
import com.fluxtion.runtime.stream.helpers.Mappers;
import com.fluxtion.runtime.stream.helpers.Peekers;

/**
 * Quick start example, run main method and view the output.
 * <P>
 *
 * Creates a processing graph that monitors trade pnl and issues stop loss/take profit orders when the profit falls
 * outside a range for trading in instrument "BTC". The example demonstrates:
 * <ul>
 *     <li>Building a graph with declarative streams</li>
 *     <li>Binds a user class into the graph for imperative programming</li>
 *     <li>Sending events to the generated graph</li>
 *     <li>Peeks into the graph publish various node states to the console</li>
 * </ul>
 * <P>
 *
 * Incoming events processed:
 * <ul>
 *     <li>{@link Trade} </li>
 *     <li>{@link PriceUpdate}</li>
 *     <li>{@link OrderDone}</li>
 * </ul>
 *
 * Filters {@link Trade} amd {@link PriceUpdate} for {@link Instrument#getInstrument()} == "BTC"
 * The graph maintains the state of several nodes that are updated with incoming events. The node calculations are
 * defined with the stream functional api. A user class, {@link ProfitAndLossTrader} is integrated in the graph and bound
 * to the outputs of a sub-set of stream nodes.
 * <p>
 *
 * The declarative streaming calculation node calculations:
 * <ul>
 *     <li>btcTradeStream -  a stream of trades that are filtered for instrument "BTC"</li>
 *     <li>btcMidPriceStream -  a stream of mid prices that are filtered for instrument "BTC", the initial value is Double.NaN</li>
 *     <li>cumulativeTradedVolume - extracts a trades volume from btcTradeStream, and keeps a cumulative sum.
 *     Pushes the result to {@link ProfitAndLossTrader#setAssetPosition(int)}. This is a stateful calculation</li>
 *     <li>assetValue - The current value of the assets at current market price. Multiplies cumulativeTradedVolume by
 *     btcMidPriceStream. This is a stateful calculation</li>
 *     <li>pnl breach monitor - pnl = sum of cash position + assetValue. if(pnl outside range) then notify the
 *     ProfitAndLossTrader of the breach</li>
 * </ul>
 *
 * The Profit and loss trader will issue a hedge trade on a pnl breach. Only one hedge trade can be active in the market,
 * even if additional breaches are reached. When an {@link OrderDone} event is received by the ProfitAndLossTrader then
 * additional hedging orders can be issued.
 */
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
        processor.onEvent(new Trade("BTC", 30, 3.5));
        processor.onEvent(new OrderDone());
        processor.onEvent(new Trade("BTC", 30, 3.5));
        processor.onEvent(new Trade("BTC", -300, 3));
        processor.onEvent(new PriceUpdate("BTC", 1, 2.0));
        processor.onEvent(new OrderDone());
        processor.onEvent(new PriceUpdate("BTC", 5, 7.0));
        processor.onEvent(new Trade("BTC", -60, 6));
    }

    private static void buildPnLControl(SEPConfig cfg){
        ProfitAndLossTrader pnlTrader = new ProfitAndLossTrader();

        var btcTradeStream = EventFlow.subscribe(Trade.class)
                .peek(Peekers.console("-----------------------------------\n{}"))
                .filter(Main::filterBTCInstrument);

        var btcMidPriceStream = EventFlow.subscribe(PriceUpdate.class)
                .peek(Peekers.console("-----------------------------------\n{}"))
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
                .map(Mappers.MULTIPLY_DOUBLES, btcMidPriceStream)
                .peek(Peekers.console("BTC position mark to market:{}"));

        //calculate pnl and push to ProfitAndLossTrader if pnl limits are breached
        btcTradeStream
                .mapToDouble(Trade::getVolume)
                .map(d -> d * -1)
                .map(Mappers.MULTIPLY_DOUBLES, btcTradeStream.mapToDouble(Trade::getPrice))
                .map(new Mappers.SumDouble()::add)
                .peek(Peekers.console("cash position:{}"))
                .map(Mappers.ADD_DOUBLES, assetValue)
                .peek(Peekers.console("trading pnl:{}"))
                .filter(pnl -> pnl < -300 || pnl > 300)
                .push(pnlTrader::pnlBreach);
    }

    public static Boolean filterBTCInstrument(Instrument trade) {
        return trade.getInstrument().equals("BTC");
    }

}
