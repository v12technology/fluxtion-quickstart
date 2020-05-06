/*
 * Copyright (C) 2018 V12 Technology Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program.  If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.fluxtion.quickstart.roomsensor.generated;

import com.fluxtion.api.StaticEventProcessor;
import com.fluxtion.api.lifecycle.BatchHandler;
import com.fluxtion.api.lifecycle.Lifecycle;
import com.fluxtion.ext.streaming.api.IntFilterEventHandler;
import com.fluxtion.ext.streaming.api.MergingWrapper;
import com.fluxtion.ext.streaming.api.log.LogControlEvent;
import com.fluxtion.ext.streaming.api.stream.SerialisedFunctionHelper.LambdaFunction;
import com.fluxtion.ext.streaming.api.stream.StreamOperator.ConsoleLog;
import com.fluxtion.ext.streaming.api.test.BooleanFilter;
import com.fluxtion.ext.streaming.api.window.CountReset;
import com.fluxtion.ext.text.api.csv.ValidationLogSink;
import com.fluxtion.ext.text.api.csv.ValidationLogger;
import com.fluxtion.ext.text.api.event.CharEvent;
import com.fluxtion.ext.text.api.event.EofEvent;
import com.fluxtion.quickstart.roomsensor.SensorMonitor.SensorReading;
import com.fluxtion.quickstart.roomsensor.SensorMonitor.TempertureController;
import java.util.Arrays;

/*
 * <pre>
 * generation time   : 2020-05-06T21:00:10.399636800
 * generator version : 2.5.1
 * api version       : 2.5.1
 * </pre>
 * @author Greg Higgins
 */
@SuppressWarnings({"deprecation", "unchecked"})
public class RoomSensorSEP implements StaticEventProcessor, BatchHandler, Lifecycle {

  //Node declarations
  private final IntFilterEventHandler handlerSensorReading_ =
      new IntFilterEventHandler(2147483647, SensorReading.class);
  private final LambdaFunction lambdaFunction_8 =
      new LambdaFunction("lambda$buildSensorProcessor$ca54918b$1_0");
  private final SensorReadingCsvDecoder0 sensorReadingCsvDecoder0_0 =
      new SensorReadingCsvDecoder0();
  private final MergingWrapper mergingWrapper_1 = new MergingWrapper(SensorReading.class);
  public final ConsoleLog consoleMsgW_1 = new ConsoleLog(mergingWrapper_1, " -> \t");
  private final GroupBy_3 groupBy_3_3 = new GroupBy_3();
  private final CountReset countReset_4 = new CountReset(groupBy_3_3, 3);
  private final BooleanFilter booleanFilter_5 = new BooleanFilter(groupBy_3_3, countReset_4);
  public final ConsoleLog consoleMsgW_2 = new ConsoleLog(booleanFilter_5, "readings in window : ");
  private final Map_collection_With_warningSensors0 map_collection_With_warningSensors0_7 =
      new Map_collection_With_warningSensors0();
  private final Filter_Collection_By_apply0 filter_Collection_By_apply0_9 =
      new Filter_Collection_By_apply0();
  public final ConsoleLog consoleMsgW_3 =
      new ConsoleLog(filter_Collection_By_apply0_9, "**** WARNING **** sensors to investigate:");
  private final Push_Collection_To_investigateSensors0 push_Collection_To_investigateSensors0_12 =
      new Push_Collection_To_investigateSensors0();
  private final TempertureController tempertureController_11 = new TempertureController();
  private final ValidationLogger validationLogger_13 = new ValidationLogger("validationLog");
  private final ValidationLogSink validationLogSink_14 = new ValidationLogSink("validationLogSink");
  //Dirty flags
  private boolean isDirty_booleanFilter_5 = false;
  private boolean isDirty_countReset_4 = false;
  private boolean isDirty_filter_Collection_By_apply0_9 = false;
  private boolean isDirty_groupBy_3_3 = false;
  private boolean isDirty_handlerSensorReading_ = false;
  private boolean isDirty_map_collection_With_warningSensors0_7 = false;
  private boolean isDirty_mergingWrapper_1 = false;
  private boolean isDirty_push_Collection_To_investigateSensors0_12 = false;
  private boolean isDirty_sensorReadingCsvDecoder0_0 = false;
  //Filter constants

  public RoomSensorSEP() {
    mergingWrapper_1.wrappedNodes.add(handlerSensorReading_);
    mergingWrapper_1.wrappedNodes.add(sensorReadingCsvDecoder0_0);
    consoleMsgW_1.setMethodSuppliers(Arrays.asList());
    consoleMsgW_2.setMethodSupplier("collection");
    consoleMsgW_2.setMethodSuppliers(Arrays.asList("collection"));
    consoleMsgW_3.setMethodSuppliers(Arrays.asList());
    countReset_4.setCount(0);
    validationLogSink_14.setPublishLogImmediately(true);
    validationLogger_13.logSink = validationLogSink_14;
    filter_Collection_By_apply0_9.setAlwaysReset(false);
    filter_Collection_By_apply0_9.setNotifyOnChangeOnly(false);
    filter_Collection_By_apply0_9.setResetImmediate(true);
    filter_Collection_By_apply0_9.setValidOnStart(false);
    filter_Collection_By_apply0_9.filterSubject = map_collection_With_warningSensors0_7;
    filter_Collection_By_apply0_9.source_0 = map_collection_With_warningSensors0_7;
    filter_Collection_By_apply0_9.f = lambdaFunction_8;
    groupBy_3_3.mergingWrapper0 = mergingWrapper_1;
    map_collection_With_warningSensors0_7.setAlwaysReset(false);
    map_collection_With_warningSensors0_7.setNotifyOnChangeOnly(false);
    map_collection_With_warningSensors0_7.setResetImmediate(true);
    map_collection_With_warningSensors0_7.setValidOnStart(false);
    map_collection_With_warningSensors0_7.filterSubject = booleanFilter_5;
    push_Collection_To_investigateSensors0_12.filterSubject = filter_Collection_By_apply0_9;
    push_Collection_To_investigateSensors0_12.f = tempertureController_11;
    sensorReadingCsvDecoder0_0.errorLog = validationLogger_13;
  }

  @Override
  public void onEvent(Object event) {
    switch (event.getClass().getName()) {
      case ("com.fluxtion.ext.streaming.api.log.LogControlEvent"):
        {
          LogControlEvent typedEvent = (LogControlEvent) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.ext.text.api.event.CharEvent"):
        {
          CharEvent typedEvent = (CharEvent) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.ext.text.api.event.EofEvent"):
        {
          EofEvent typedEvent = (EofEvent) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.quickstart.roomsensor.SensorMonitor$SensorReading"):
        {
          SensorReading typedEvent = (SensorReading) event;
          handleEvent(typedEvent);
          break;
        }
      case ("java.lang.String"):
        {
          String typedEvent = (String) event;
          handleEvent(typedEvent);
          break;
        }
    }
  }

  public void handleEvent(LogControlEvent typedEvent) {
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.ext.streaming.api.log.LogControlEvent] filterString:[CHANGE_LOG_PROVIDER]
      case ("CHANGE_LOG_PROVIDER"):
        validationLogSink_14.controlLogProvider(typedEvent);
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(CharEvent typedEvent) {
    //Default, no filter methods
    isDirty_sensorReadingCsvDecoder0_0 = sensorReadingCsvDecoder0_0.charEvent(typedEvent);
    if (isDirty_sensorReadingCsvDecoder0_0) {
      mergingWrapper_1.mergeWrapperUpdate(sensorReadingCsvDecoder0_0);
    }
    if (isDirty_handlerSensorReading_ | isDirty_sensorReadingCsvDecoder0_0) {
      isDirty_mergingWrapper_1 = mergingWrapper_1.updated();
      if (isDirty_mergingWrapper_1) {
        groupBy_3_3.updatemergingWrapper0(mergingWrapper_1);
      }
    }
    if (isDirty_mergingWrapper_1) {
      consoleMsgW_1.log();
    }
    if (isDirty_mergingWrapper_1) {
      isDirty_groupBy_3_3 = groupBy_3_3.updated();
    }
    if (isDirty_groupBy_3_3) {
      isDirty_countReset_4 = countReset_4.newBucket();
    }
    if (isDirty_countReset_4) {
      isDirty_booleanFilter_5 = booleanFilter_5.updated();
    }
    if (isDirty_booleanFilter_5) {
      consoleMsgW_2.log();
    }
    if (isDirty_booleanFilter_5) {
      isDirty_map_collection_With_warningSensors0_7 =
          map_collection_With_warningSensors0_7.onEvent();
    }
    if (isDirty_map_collection_With_warningSensors0_7) {
      isDirty_filter_Collection_By_apply0_9 = filter_Collection_By_apply0_9.onEvent();
    }
    if (isDirty_filter_Collection_By_apply0_9) {
      consoleMsgW_3.log();
    }
    if (isDirty_filter_Collection_By_apply0_9) {
      isDirty_push_Collection_To_investigateSensors0_12 =
          push_Collection_To_investigateSensors0_12.onEvent();
    }
    //event stack unwind callbacks
    if (isDirty_groupBy_3_3) {
      countReset_4.resetIfNecessary();
    }
    afterEvent();
  }

  public void handleEvent(EofEvent typedEvent) {
    //Default, no filter methods
    isDirty_sensorReadingCsvDecoder0_0 = sensorReadingCsvDecoder0_0.eof(typedEvent);
    if (isDirty_sensorReadingCsvDecoder0_0) {
      mergingWrapper_1.mergeWrapperUpdate(sensorReadingCsvDecoder0_0);
    }
    if (isDirty_handlerSensorReading_ | isDirty_sensorReadingCsvDecoder0_0) {
      isDirty_mergingWrapper_1 = mergingWrapper_1.updated();
      if (isDirty_mergingWrapper_1) {
        groupBy_3_3.updatemergingWrapper0(mergingWrapper_1);
      }
    }
    if (isDirty_mergingWrapper_1) {
      consoleMsgW_1.log();
    }
    if (isDirty_mergingWrapper_1) {
      isDirty_groupBy_3_3 = groupBy_3_3.updated();
    }
    if (isDirty_groupBy_3_3) {
      isDirty_countReset_4 = countReset_4.newBucket();
    }
    if (isDirty_countReset_4) {
      isDirty_booleanFilter_5 = booleanFilter_5.updated();
    }
    if (isDirty_booleanFilter_5) {
      consoleMsgW_2.log();
    }
    if (isDirty_booleanFilter_5) {
      isDirty_map_collection_With_warningSensors0_7 =
          map_collection_With_warningSensors0_7.onEvent();
    }
    if (isDirty_map_collection_With_warningSensors0_7) {
      isDirty_filter_Collection_By_apply0_9 = filter_Collection_By_apply0_9.onEvent();
    }
    if (isDirty_filter_Collection_By_apply0_9) {
      consoleMsgW_3.log();
    }
    if (isDirty_filter_Collection_By_apply0_9) {
      isDirty_push_Collection_To_investigateSensors0_12 =
          push_Collection_To_investigateSensors0_12.onEvent();
    }
    //event stack unwind callbacks
    if (isDirty_groupBy_3_3) {
      countReset_4.resetIfNecessary();
    }
    afterEvent();
  }

  public void handleEvent(SensorReading typedEvent) {
    //Default, no filter methods
    isDirty_handlerSensorReading_ = true;
    handlerSensorReading_.onEvent(typedEvent);
    if (isDirty_handlerSensorReading_) {
      mergingWrapper_1.mergeWrapperUpdate(handlerSensorReading_);
    }
    if (isDirty_handlerSensorReading_ | isDirty_sensorReadingCsvDecoder0_0) {
      isDirty_mergingWrapper_1 = mergingWrapper_1.updated();
      if (isDirty_mergingWrapper_1) {
        groupBy_3_3.updatemergingWrapper0(mergingWrapper_1);
      }
    }
    if (isDirty_mergingWrapper_1) {
      consoleMsgW_1.log();
    }
    if (isDirty_mergingWrapper_1) {
      isDirty_groupBy_3_3 = groupBy_3_3.updated();
    }
    if (isDirty_groupBy_3_3) {
      isDirty_countReset_4 = countReset_4.newBucket();
    }
    if (isDirty_countReset_4) {
      isDirty_booleanFilter_5 = booleanFilter_5.updated();
    }
    if (isDirty_booleanFilter_5) {
      consoleMsgW_2.log();
    }
    if (isDirty_booleanFilter_5) {
      isDirty_map_collection_With_warningSensors0_7 =
          map_collection_With_warningSensors0_7.onEvent();
    }
    if (isDirty_map_collection_With_warningSensors0_7) {
      isDirty_filter_Collection_By_apply0_9 = filter_Collection_By_apply0_9.onEvent();
    }
    if (isDirty_filter_Collection_By_apply0_9) {
      consoleMsgW_3.log();
    }
    if (isDirty_filter_Collection_By_apply0_9) {
      isDirty_push_Collection_To_investigateSensors0_12 =
          push_Collection_To_investigateSensors0_12.onEvent();
    }
    //event stack unwind callbacks
    if (isDirty_groupBy_3_3) {
      countReset_4.resetIfNecessary();
    }
    afterEvent();
  }

  public void handleEvent(String typedEvent) {
    //Default, no filter methods
    tempertureController_11.setSmsDetails(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  private void afterEvent() {
    filter_Collection_By_apply0_9.resetAfterEvent();
    isDirty_booleanFilter_5 = false;
    isDirty_countReset_4 = false;
    isDirty_filter_Collection_By_apply0_9 = false;
    isDirty_groupBy_3_3 = false;
    isDirty_handlerSensorReading_ = false;
    isDirty_map_collection_With_warningSensors0_7 = false;
    isDirty_mergingWrapper_1 = false;
    isDirty_push_Collection_To_investigateSensors0_12 = false;
    isDirty_sensorReadingCsvDecoder0_0 = false;
  }

  @Override
  public void init() {
    lambdaFunction_8.init();
    sensorReadingCsvDecoder0_0.init();
    consoleMsgW_1.init();
    groupBy_3_3.init();
    countReset_4.init();
    consoleMsgW_2.init();
    map_collection_With_warningSensors0_7.init();
    filter_Collection_By_apply0_9.init();
    consoleMsgW_3.init();
    validationLogSink_14.init();
  }

  @Override
  public void tearDown() {}

  @Override
  public void batchPause() {}

  @Override
  public void batchEnd() {}
}
