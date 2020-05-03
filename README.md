# Introduction
5 Minute tutorial to demonstrate stream data processing using Fluxtion. 
The goal is to read a sensor data stream for a set of rooms, calculate aggregate values per room and 
notify a user class when a room breaches set temperature criteria. The user class
will send an SMS alert when notified, if a number has been registered. 

## Requirements
 - Read room sensor temperature as a stream of csv records or as instances of SensorReading events. 
 - Merge csv records and SensorReading instances into a single event stream for processing
 - The event stream can be infinite
 - For each room calculate the max and average temperature
 - Run a tumbling window, zeroing all room values every 3 readings
 - Register a user class with the stream processor to act as a controller of an external system
 - If a room has an average of > 60 and max of > 90 then
	 - Log a warning
	 - A user class(TempertureController) will attempt to send an SMS listing rooms to investigate
 - Register an SMS endpoint with the controller by sending a String as an event into the processor
 - The SMS number can be updated in realtime

## Solution description
### Dependencies
Maven:
```xml
    <dependency>
        <groupId>com.fluxtion.extension</groupId>
        <artifactId>fluxtion-text-builder</artifactId>
        <version>2.5.1</version>
    </dependency>
```
Groovy:
```groovy
implementation 'com.fluxtion.extension:fluxtion-text-builder:2.5.1'
```
### Building the event processor
The [SensorMonitor](src/main/java/com/fluxtion/quickstart/roomsensor/SensorMonitor.java) 
builds a streaming processing engine in the main method using the [reuseOrBuild](https://github.com/v12technology/fluxtion/blob/a15f9bc6e28ed7071be16795d6813724454b8f11/generator/src/main/java/com/fluxtion/generator/compiler/InprocessSepCompiler.java#L158) function. A  method reference is passed to the builder to reduce code noise.

```java
StaticEventProcessor processor = reuseOrBuild("RoomSensorSEP", "com.fluxtion.quickstart.roomsensor.generated", 
SensorMonitor::buildSensorProcessor);
```
 The two string parameters are used as the fully qualified name of the generated stream processing class. 
The call to reuseOrBuild checks the classpath for a class that matches the fully qualified name. 
If no class can be loaded for that fqn, then a new stream processor is generated. 
### Defining the calculation
The builder method constructs the processor with the following definition: 
```java
public static void buildSensorProcessor(SEPConfig cfg) {
    //merge csv marshller and SensorReading instance events
    Wrapper<SensorReading> sensorData = merge(select(SensorReading.class),
            csvMarshaller(SensorReading.class).build()).console(" -> \t");
    //group by sensor and calculate max, average
    GroupBy<SensorReadingDerived> sensors = groupBy(sensorData, SensorReading::getSensorName, SensorReadingDerived.class)
            .init(SensorReading::getSensorName, SensorReadingDerived::setSensorName)
            .max(SensorReading::getValue, SensorReadingDerived::setMax)
            .avg(SensorReading::getValue, SensorReadingDerived::setAverage)
            .build();
    //tumble window (count=3), warning if avg > 60 && max > 90 in the window for a sensor
    tumble(sensors, 3).console("readings in window : ", GroupBy::collection)
            .map(SensorMonitor::warningSensors, GroupBy::collection)
            .filter(c -> c.size() > 0)
            .console("**** WARNING **** sensors to investigate:")
            .push(new TempertureController()::investigateSensors);
}
```
### Integrated user classes 
The builder refers to two helper instances that define the input and output datatypes:
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public static class SensorReading {

    private String sensorName;
    private int value;

    @Override
    public String toString() {
        return sensorName + ":" + value;
    }
}

@Data
public static class SensorReadingDerived {

    private String sensorName;
    private int max;
    private double average;

    @Override
    public String toString() {
        return "(" + sensorName + "  max:" + max + " average:" + average + ")";
    }
}
```
A user supplied mapping function converts the aggregated sensor data for all 
rooms and creates a collection of room names that require investigation:
```java
public static Collection<String> warningSensors(Collection<SensorReadingDerived> readings) {
    return readings.stream()
            .filter(s -> s.getMax() > 90).filter(s -> s.getAverage() > 60)
            .map(SensorReadingDerived::getSensorName)
            .collect(Collectors.toList());
}
```

A user supplied controller instance is registered with the stream processor. When 
the list of rooms to investigate is > 0, the list is pushed by the processor to 
the user controller class. The controller class also annotates a method as receiving
a String as an event. The processor will route String events to the controller class
 ```java
public static class TempertureController {

    private String smsDetails;

    public void investigateSensors(Collection<String> sensors) {
        if (smsDetails == null) {
            System.out.println("NO SMS details registered, controller impotent");
        } else {
            System.out.println("SMS:" + smsDetails + " investigate:" + sensors);
        }
    }

    @EventHandler
    public void setSmsDetails(String details) {
        System.out.println("Temp controller registering sms details:" + details);
        this.smsDetails = details;
    }
}
```

## Running the application
Clone the application and execute the sensorquickstart.jar in the dist directory. 
The application processes the file temperatureData.csv as an input in place of real sensor source. 
An alert condition is met, but no SMS endoint is registered so the controller cannot send a message.

After reading the csv file SensorReading events are programatically sent to the processor, 
to register an SMS number and create an alert condition. In this case the 
controller can now send an SMS message. 
```bat
git clone https://github.com/v12technology/fluxtion-quickstart.git
cd fluxrtion-quickstart
java  -Dfluxtion.cacheDirectory=fluxtion -jar dist\sensorquickstart.jar
21:40:45.991 [main] INFO  c.f.generator.compiler.SepCompiler - generated sep: C:\quickstart\fluxtion\source\com\fluxtion\quickstart\roomsensor\generated\RoomSensorSEP.java
 ->     bathroom:45
 ->     living:78
 ->     bed:43
readings in window : [(bathroom  max:45 average:45.0), (living  max:78 average:78.0), (bed  max:43 average:43.0)]
 ->     bed:23
 ->     bathroom:19
 ->     bed:34
readings in window : [(bed  max:34 average:28.5), (bathroom  max:19 average:19.0)]
 ->     living:89
 ->     bed:23
 ->     living:44
readings in window : [(living  max:89 average:66.5), (bed  max:23 average:23.0)]
 ->     living:36
 ->     living:99
 ->     living:56
readings in window : [(living  max:99 average:63.666666666666664)]
**** WARNING **** sensors to investigate:[living]
NO SMS details registered, controller impotent
Temp controller registering sms details:0800-1-HELP-ROOMTEMP
 ->     living:36
 ->     living:99
 ->     living:56
readings in window : [(living  max:99 average:63.666666666666664)]
**** WARNING **** sensors to investigate:[living]
SMS:0800-1-HELP-ROOMTEMP investigate:[living]
```
### Cached compilation
The application generates a solution in the cache directory fluxtion, set with 
system property: -Dfluxtion.cacheDirectory=fluxtion. The fluxtion directory contains three sub-directories:
 - classes - compiled classes implementing the stream processing requirements
 - resources - meta-data describing the processor
 - sources - The java source files used to generate the classes 

Executing the jar a second time sees a significant reduction in execution time as the 
application uses the compiled processor from the first run. The cached classes are loaded internally by the reuseOrBuild method. Using the cached compiled classes gives an almost instant response to the event stream in the application.

Deleting the cache directory will cause the regeneration and compilation of the solution. 


