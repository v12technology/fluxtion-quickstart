# Introduction
5 Minute tutorial to demonstrate processing of streaming data using Fluxtion. 
The goal is to read sensor data for a set of rooms, calculate aggregate values per room and 
notify a user class when a room breaches set temperature criteria.

## Requirements
 - Read room sensor temperature as a csv character stream or as instances of SensorReading events. 
 - Merge csv and SensorReading's into a single event stream for processing
 - The event stream can be infinite
 - For each room calculate the max and average temperature
 - Run a tumbling window, zeroing all room values every 3 readings
 - Register a user class with the stream processor to act as a controller of an external system
 - If a room has an average of > 60 and max of > 90 then
	 - Log a warning
	 - A user class(TempertureController) will attempt to send an SMS listing rooms to investigate
 - Register an SMS endpoint with the controller by sending a String as an event into the processor

## Code description
The application depeneds upon the fluxtion-text-builder library. This brings in all
the dependencies required for fluxtion. The maven dependency is:

```xml
    <dependency>
        <groupId>com.fluxtion.extension</groupId>
        <artifactId>fluxtion-text-builder</artifactId>
        <version>${fluxtion.ver}</version>
    </dependency>
```

The [SensorMonitor](src/main/java/com/fluxtion/quickstart/roomsensor/SensorMonitor.java) 
builds a streaming processing engine in the main, referring to a builder using a method reference.
 The two string parameters are used as the fully qualified name of the generated stream processing class.

```java
StaticEventProcessor processor = reuseOrBuild("RoomSensorSEP", "com.fluxtion.quickstart.roomsensor.generated", SensorMonitor::buildSensorProcessor);

```

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
Clone the application and execute the sensorquickstart.jar in the dist directory. The application will 
process the file temperatureData.csv as an input. After the csv file is read 
sensor reading events are programatically sent to the processor.
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
The application generates a solution in the cache directory fluxtion , ready for a second run, The directory contains three sub-directories:
 - classes - compiled classes implementing the stream processing requirements
 - resources - meta-data describing the processor
 - sources - The java source files used to generate the classes 

Executing the jar a second time sees a significant reduction in execution time as the processor has been compiled ahead of time and is executed immediately. Deleting the cache directory will cause the regeneration and compilation of the solution. 



