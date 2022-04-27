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
 - Run the [Main](src/main/java/com/fluxtion/learning/quicktart/stoploss/Main.java) java class from ide or maven

Example output displayed on the console when building and running with maven

```text
D:\> git clone https://github.com/v12technology/fluxtion-quickstart.git
Cloning into 'fluxtion-quickstart'...
D:\> cd .\fluxtion-quickstart\
D:\fluxtion-quickstart> mvn install
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
The input events that drive the program are:
```java
processor.onEvent(new TradeEvent("BTC", 100, 3));
processor.onEvent(new TradeEvent("NOT-BTC", 100, 35_000));
processor.onEvent(new PriceUpdateEvent("BTC", 2, 3.0));
processor.onEvent(new TradeEvent("BTC", 200, 4));
processor.onEvent(new TradeEvent("BTC", 30, 3.5));
processor.onEvent(new OrderDoneEvent());
processor.onEvent(new TradeEvent("BTC", 30, 3.5));
processor.onEvent(new TradeEvent("BTC", -300, 3));
processor.onEvent(new PriceUpdateEvent("BTC", 1, 2.0));
processor.onEvent(new OrderDoneEvent());
processor.onEvent(new PriceUpdateEvent("BTC", 5, 7.0));
processor.onEvent(new TradeEvent("BTC", -60, 6));
```

# Program description
Create a processing graph that monitors trade pnl and issues stop loss/take profit orders when the profit falls                
outside a range for trading in instrument "BTC". Events are fed into the graph, the processing of events 
drives calculations and actions. This example demonstrates:
 - Building a graph with functional stream api                                                                         
 - Binds a user class into the graph for imperative programming                                                      
 - Sending events to the generated graph      
 - Calculating realtime values and conditionally invoking actions 
 - Peeking into the graph and publishing selected node states to the console                                                                                                                            

**Incoming events processed:**                                                                                                                          
 - [TradeEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/TradeEvent.java) An execution of a trade
 - [PriceUpdateEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/PriceUpdateEvent.java) Update of the price of an asset in the market
 - [OrderDoneEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/OrderDoneEvent.java) Notifies that a hedging stop/loss order has completed

## Building the graph 
The graph is built in the [Main](src/main/java/com/fluxtion/learning/quicktart/stoploss/Main.java) method and initialised
ready to process events with:
```java
EventProcessor tradeController = Fluxtion.interpret(Main::buildPnLControl);
tradeController.init();
```

The actual graph is constructed in the method ```private static void buildPnLControl(SEPConfig cfg)``` the supplied 
SEPConfig can be used to customise the graph before generation. In this example no customisation is required.
 
The node calculations are defined with the stream functional api. A user class, [ProfitAndLossTrader](src/main/java/com/fluxtion/learning/quicktart/stoploss/ProfitAndLossTrader.java)
is integrated in the graph and bound to the outputs of a sub-set of stream nodes. The ProfitAndLossTrader is responsible
for issuing hedging orders with imperative logic.

Node values calculated using the streaming api:
 - btcTradeStream -  a stream of trades that are filtered for instrument "BTC"
 - btcMidPriceStream -  a stream of mid-prices that are filtered for instrument "BTC", the initial value is Double.NaN
 - cumulativeTradedVolume - extracts a Trade's volume from btcTradeStream, and maintains a cumulative sum.                      
    Pushes the result to ```ProfitAndLossTrader#setAssetPosition(int)```.
 - assetValue - The current value of the assets at current market price. Multiplies cumulativeTradedVolume by              
    btcMidPriceStream.
 - pnl breach monitor - pnl = sum of cash position + assetValue. if(pnl outside range) then notify the                     
    ProfitAndLossTrader of the breach

The Profit and loss trader will issue a hedge trade on a pnl breach. Only one hedge trade can be active in the market,          
even if additional breaches are reached. When an [OrderDoneEvent](src/main/java/com/fluxtion/learning/quicktart/stoploss/OrderDoneEvent.java)  
event is received by the ProfitAndLossTrader then additional hedging orders can be issued.  

### Data stream functional programming
Node can be defined using a stream like api. Each node is a live element in the graph and 
can be used to drive other nodes.

#### Subscribing and filtering
An entry point to the graph is an event which can be subscribed to using the event type as an argument. A filter 
operation can be applied to the stream as a lambda or method reference. Only matching events will propagate through the 
graph. The statement below subscribes to TradeEvents, and propagates events where instrumentName == "BTC"

```java
var btcTradeStream = EventFlow.subscribe(TradeEvent.class)
        .peek(Peekers.console("-----------------------------------\n{}"))
        .filter(Main::filterBTCInstrument);
```
#### Chaining nodes and pushing values
The result of a previous node can be used to drive downstream nodes. If an upstream node is re-used a processing 
graph will be created as opposed to a simple pipeline. The upstream filtered trade stream, btcTradeStream, is used to 
calculate a cumulative trade volume for BTC by applying a pipeline of mapping functions. The cumulative sum is pushed
into the application instance pnlTrader#setAssetPosition

```java
var cumulativeTradedVolume = btcTradeStream
        .mapToInt(TradeEvent::getVolume)
        .map(new Mappers.SumInt()::add)
        .push(pnlTrader::setAssetPosition)
        .peek(Peekers.console("BTC position:{}"));
```

### Imperative logic and integrating user classes

Not all logic is efficiently declared with functional programming constructs. A user class can be added to the graph 
and imperative logic triggered in a callback method. 

The ProfitAndLossTrader has two callback methods, pnl value is pushed from an upstream node, and an event handler for
OrderDoneEvent's is wired in using an @OnEventHandler annotation. Imperative logic in the pnlBreach determines 
whether to issue an order or not.

```java
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
```

## Processing events
Once the graph has been built and initialised it is ready to process events. Events instances are fed into the 
processor ```processor.onEvent(event)```. All dispatch and routing of method calls is taken care of by the processor.

In this example the sendData method sends a sample set of events for processing, in this case events are simple POJO's:

```java
    private static void sendData(EventProcessor processor){
        processor.onEvent(new TradeEvent("BTC", 100, 3));
        processor.onEvent(new TradeEvent("NOT-BTC", 100, 35_000));
        processor.onEvent(new PriceUpdateEvent("BTC", 2, 3.0));
        processor.onEvent(new TradeEvent("BTC", 200, 4));
        processor.onEvent(new TradeEvent("BTC", 30, 3.5));
        processor.onEvent(new OrderDoneEvent());
        processor.onEvent(new TradeEvent("BTC", 30, 3.5));
        processor.onEvent(new TradeEvent("BTC", -300, 3));
        processor.onEvent(new PriceUpdateEvent("BTC", 1, 2.0));
        processor.onEvent(new OrderDoneEvent());
        processor.onEvent(new PriceUpdateEvent("BTC", 5, 7.0));
        processor.onEvent(new TradeEvent("BTC", -60, 6));
    }
```