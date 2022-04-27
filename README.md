# Introduction
5 Minute tutorial to demonstrate stream data processing using Fluxtion. 
The goal is to monitor trades in a specific asset and issue stop loss or take profit orders when a the profit or loss 
exceeds a set value. Events are fed into the system, trade values are calculated and order actions are invoked if
necessary.

To get benefit out of this tutorial you should have:

 - A passing understanding of [stream processing](https://dzone.com/articles/what-is-stream-processing-a-gentle-introduction)
 - Intermediate Java coding skills combined with basic knowledge of git and maven

# Running the project
 - Install git, maven and Java version 11 or higher
 - Clone the repository from version control
 - build using maven install 
 - Run the main java class from ide or maven

Example output displayed on the console when building and running with maven

```text
D:\fluxtion-quickstart> mvn install

... removed maven build output for clarity

D:\fluxtion-quickstart> mvn exec:java

-----------------------------------
TradeEvent(instrument=BTC, volume=100, price=3.0)
BTC position:100
BTC position mark to market:NaN
cash position:-300.0
trading pnl:NaN
-----------------------------------
TradeEvent(instrument=NOT-BTC, volume=100, price=35000.0)
-----------------------------------
PriceUpdateEvent(instrument=BTC, bid=2.0, offer=3.0)
BTC position mark to market:250.0
trading pnl:-50.0
-----------------------------------
TradeEvent(instrument=BTC, volume=200, price=4.0)
BTC position:300
BTC position mark to market:750.0
cash position:-1100.0
trading pnl:-350.0
HEDGE pnl breach -350.0 - send a trade to clear position:300
-----------------------------------
TradeEvent(instrument=BTC, volume=30, price=3.5)
BTC position:330
BTC position mark to market:825.0
cash position:-1205.0
trading pnl:-380.0
NO HEDGE pnl breach -380.0 - live order still not done
-----------------------------------
Hedge order complete
-----------------------------------
TradeEvent(instrument=BTC, volume=30, price=3.5)
BTC position:360
BTC position mark to market:900.0
cash position:-1310.0
trading pnl:-410.0
HEDGE pnl breach -410.0 - send a trade to clear position:360
-----------------------------------
TradeEvent(instrument=BTC, volume=-300, price=3.0)
BTC position:60
BTC position mark to market:150.0
cash position:-410.0
trading pnl:-260.0
-----------------------------------
PriceUpdateEvent(instrument=BTC, bid=1.0, offer=2.0)
BTC position mark to market:90.0
trading pnl:-320.0
NO HEDGE pnl breach -320.0 - live order still not done
-----------------------------------
Hedge order complete
-----------------------------------
PriceUpdateEvent(instrument=BTC, bid=5.0, offer=7.0)
BTC position mark to market:360.0
trading pnl:-50.0
-----------------------------------
TradeEvent(instrument=BTC, volume=-60, price=6.0)
BTC position:0
BTC position mark to market:0.0
cash position:-50.0
trading pnl:-50.0
```

# Program description
Create a processing graph that monitors trade pnl and issues stop loss/take profit orders when the profit falls                
outside a range for trading in instrument "BTC". The example demonstrates:
 - Building a graph with declarative streams                                                                         
 - Binds a user class into the graph for imperative programming                                                      
 - Sending events to the generated graph      
 - Calculates values and conditionally invokes actions 
 - Peeks into the graph publishing various node states to the console                                                                                                                            

**Incoming events processed:**                                                                                                                          
 - [TradeEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/TradeEvent.java)                                                                                              
 - [PriceUpdateEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/PriceUpdateEvent.java)                                                                                        
 - [OrderDoneEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/OrderDoneEvent.java)                                                                                           

## Building the graph 
The graph is built in the [Main](src/main/java/com/fluxtion/learning/quicktart/stoploss/Main.java) method and initialised
ready to process events with:
```java
EventProcessor tradeController = Fluxtion.interpret(Main::buildPnLControl);
tradeController.init();
```

The actual graph is constructed in the method ```private static void buildPnLControl(SEPConfig cfg)``` the supplied 
SEPConfig can be used to customise the graph before generation. In this example no customisation is required.
 
The graph maintains the state of nodes that are updated with incoming events. The node calculations are                 
defined with the stream functional api. A user class, [ProfitAndLossTrader](src/main/java/com/fluxtion/learning/quicktart/stoploss/ProfitAndLossTrader.java)
is integrated in the graph and bound to the outputs of a sub-set of stream nodes. The ProfitAndLossTrader is responsible
for issuing hedging orders with imperative logic.

Node values calculated using the streaming api and functional prograaming approach, similar to java 8 streams:
 - btcTradeStream -  a stream of trades that are filtered for instrument "BTC"
 - btcMidPriceStream -  a stream of mid prices that are filtered for instrument "BTC", the initial value is Double.NaN
 - cumulativeTradedVolume - extracts a trades volume from btcTradeStream, and keeps a cumulative sum.                      
    Pushes the result to ```ProfitAndLossTrader#setAssetPosition(int)```. This is a stateful calculation
 - assetValue - The current value of the assets at current market price. Multiplies cumulativeTradedVolume by              
    btcMidPriceStream. This is a stateful calculation
 - pnl breach monitor - pnl = sum of cash position + assetValue. if(pnl outside range) then notify the                     
    ProfitAndLossTrader of the breach

The Profit and loss trader will issue a hedge trade on a pnl breach. Only one hedge trade can be active in the market,          
even if additional breaches are reached. When an [OrderDoneEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/OrderDoneEvent.java)  
event is received by the ProfitAndLossTrader then additional hedging orders can be issued.                                                                                        

 


