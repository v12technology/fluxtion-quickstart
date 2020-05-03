/*
 * Copyright (c) 2020, V12 Technology Ltd.
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program.  If not, see 
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.fluxtion.quickstart.roomsensor;

import com.fluxtion.api.StaticEventProcessor;
import com.fluxtion.api.annotations.EventHandler;
import com.fluxtion.builder.node.SEPConfig;
import static com.fluxtion.ext.streaming.api.MergingWrapper.merge;
import com.fluxtion.ext.streaming.api.Wrapper;
import com.fluxtion.ext.streaming.api.group.GroupBy;
import static com.fluxtion.ext.streaming.builder.factory.EventSelect.select;
import static com.fluxtion.ext.streaming.builder.factory.WindowBuilder.tumble;
import static com.fluxtion.ext.streaming.builder.group.Group.groupBy;
import com.fluxtion.ext.text.api.util.CharStreamer;
import static com.fluxtion.ext.text.builder.csv.CsvMarshallerBuilder.csvMarshaller;
import static com.fluxtion.generator.compiler.InprocessSepCompiler.reuseOrBuild;
import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An example to demonstrate a mixture of Fluxtion functionality from CSV
 * marshalling to window based calculations. The generated stream processor
 * provides the behaviour:
 * <ul>
 * <li>Read room sensor temperature events as a csv character stream or instance
 * SensorReading events
 * <li>For each room calculate the max and average temperature individually
 * <li>Run a tumbling window, zeroing all room values every 3 readings
 * <li>Register a user class as an instance in the processor to act as a
 * controller
 * <li>If a room has an average of > 60 and max of >90 then:
 * <ul>
 * <li>log a warning
 * <li>A user class(TempertureController) will send an SMS of rooms to
 * investigate if an SMS number is registered
 * </ul>
 * <li>Register an SMS endpoint with ith the controller by sending a String as
 * an event
 * </ul>
 * <p>
 *
 * The example demonstrates:
 * <ul>
 * <li>Processing an infinite stream of herogeneous event types
 * <li>Type safe construction using method references
 * <li>Auto generation of CSV marshaller
 * <li>Merging events of the same type from different source into a single
 * stream
 * <li>Handling heterogeneous event types, each with their own execution path
 * <li>GroupBy calculating derived data
 * <li>Tumbling windows operating on Grouped data, resetting state based on
 * count
 * <li>Stateful pre-defined calculations
 * <li>User supplied arbitrary mapping calculation
 * <li>User lambda functions for filtering
 * <li>Logging to console for debug while developing
 * <li>Integrating a user instance into the execution graph
 * <li>Sending data events to a user instance via onEvent
 * <li>Propagating updates only when tests are valid
 * <li>Pushing data to a user instance, removing the pull from user code.
 * <li>Any class can act as an input event, even a java.lang.String
 * <li>Mixing of imperative and declarative code
 * <li>Translation of reference to primitive types with no allocations
 * <li>Generating of the processor as Java code, cached for use.
 * </ul>
 *
 * For the first run Fluxtion generates the static event processor in:
 * [project_roo]\target\generated-sources\fluxtion, subsequent runs will use the
 * cached processor. An image representing the processing graph can be found in
 * the
 * [project_roo]\src\main\resources\com\fluxtion\quickstart\roomsensor\generated
 * folder
 *
 *
 * @author Greg Higgins greg.higgins@v12technology.com
 */
public class SensorMonitor {

    public static void main(String[] args) throws Exception {
        StaticEventProcessor processor = reuseOrBuild("RoomSensorSEP",
                "com.fluxtion.quickstart.roomsensor.generated",
                SensorMonitor::buildSensorProcessor);
        CharStreamer.stream(new File("temperatureData.csv"), processor).sync().stream();
        processor.onEvent("0800-1-HELP-ROOMTEMP");
        processor.onEvent(new SensorReading("living", 36));
        processor.onEvent(new SensorReading("living", 99));
        processor.onEvent(new SensorReading("living", 56));
    }

    @SuppressWarnings({"unchecked", "varargs"})
    public static void buildSensorProcessor(SEPConfig cfg) {
        //merge csv marshller and SensorReading instance events
        Wrapper<SensorReading> sensorData = merge(select(SensorReading.class),
                csvMarshaller(SensorReading.class).build()).console(" -> \t");
        //group by sensor and calculate max, average
        GroupBy<SensorReadingDerived> sensors = groupBy(sensorData, SensorReading::getSensorName,
                SensorReadingDerived.class)
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

    public static Collection<String> warningSensors(Collection<SensorReadingDerived> readings) {
        return readings.stream()
                .filter(s -> s.getMax() > 90).filter(s -> s.getAverage() > 60)
                .map(SensorReadingDerived::getSensorName)
                .collect(Collectors.toList());
    }

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

}
