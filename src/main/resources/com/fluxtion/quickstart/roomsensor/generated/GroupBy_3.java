package com.fluxtion.quickstart.roomsensor.generated;

import com.fluxtion.api.SepContext;
import com.fluxtion.api.annotations.EventHandler;
import com.fluxtion.api.annotations.Initialise;
import com.fluxtion.api.annotations.NoEventReference;
import com.fluxtion.api.annotations.OnEvent;
import com.fluxtion.api.annotations.OnEventComplete;
import com.fluxtion.api.annotations.OnParentUpdate;
import com.fluxtion.ext.streaming.api.ArrayListWrappedCollection;
import com.fluxtion.ext.streaming.api.MergingWrapper;
import com.fluxtion.ext.streaming.api.WrappedCollection;
import com.fluxtion.ext.streaming.api.Wrapper;
import com.fluxtion.ext.streaming.api.group.AggregateFunctions.AggregateAverage;
import com.fluxtion.ext.streaming.api.group.AggregateFunctions.AggregateMax;
import com.fluxtion.ext.streaming.api.group.GroupBy;
import com.fluxtion.ext.streaming.api.group.GroupByIniitialiser;
import com.fluxtion.ext.streaming.api.group.GroupByTargetMap;
import com.fluxtion.quickstart.roomsensor.SensorMonitor.SensorReading;
import com.fluxtion.quickstart.roomsensor.SensorMonitor.SensorReadingDerived;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;

/**
 * generated group by holder.
 *
 * <p>target class : SensorReadingDerived
 *
 * @author Greg Higgins
 */
public final class GroupBy_3 implements GroupBy<SensorReadingDerived> {

  @NoEventReference public Object resetNotifier;
  private ArrayListWrappedCollection<SensorReadingDerived> wrappedList;
  private boolean allMatched;
  public MergingWrapper mergingWrapper0;
  private SensorReadingDerived target;
  private GroupByTargetMap<String, SensorReadingDerived, CalculationStateGroupBy_3> calcState;
  private GroupByIniitialiser<SensorReading, SensorReadingDerived> initialisermergingWrapper0;

  @OnParentUpdate("mergingWrapper0")
  public boolean updatemergingWrapper0(MergingWrapper eventWrapped) {
    SensorReading event = (SensorReading) eventWrapped.event();
    CalculationStateGroupBy_3 instance = calcState.getOrCreateInstance(event.getSensorName());
    boolean firstMatched = instance.processSource(1, initialisermergingWrapper0, event);
    allMatched = instance.allMatched();
    target = instance.target;
    {
      int value = instance.aggregateMax1;
      value = AggregateMax.maximum((int) event.getValue(), (int) value);
      target.setMax((int) value);
      instance.aggregateMax1 = value;
    }
    {
      double value = instance.aggregateAverage2;
      value =
          instance.aggregateAverage2Function.calcAverage((double) event.getValue(), (double) value);
      target.setAverage((double) value);
      instance.aggregateAverage2 = value;
    }
    if (firstMatched) {
      wrappedList.addItem(target);
    }
    return allMatched;
  }

  @OnEvent
  public boolean updated() {
    boolean updated = allMatched;
    allMatched = false;
    return updated;
  }

  @Initialise
  public void init() {
    calcState = new GroupByTargetMap<>(CalculationStateGroupBy_3.class);
    wrappedList = new ArrayListWrappedCollection<>();
    wrappedList.init();
    allMatched = false;
    target = null;
    initialisermergingWrapper0 =
        new GroupByIniitialiser<SensorReading, SensorReadingDerived>() {

          @Override
          public void apply(SensorReading source, SensorReadingDerived target) {
            target.setSensorName((java.lang.String) source.getSensorName());
          }
        };
  }

  @Override
  public SensorReadingDerived value(Object key) {
    return calcState.getInstance((String) key).target;
  }

  @Override
  public Collection<SensorReadingDerived> collection() {
    return wrappedList.collection();
  }

  @Override
  public <V extends Wrapper<SensorReadingDerived>> Map<String, V> getMap() {
    return (Map<String, V>) calcState.getInstanceMap();
  }

  @Override
  public SensorReadingDerived record() {
    return target;
  }

  @Override
  public Class<SensorReadingDerived> recordClass() {
    return SensorReadingDerived.class;
  }

  public GroupBy_3 resetNotifier(Object resetNotifier) {
    this.resetNotifier = resetNotifier;
    return this;
  }

  @OnParentUpdate("resetNotifier")
  public void resetNotification(Object resetNotifier) {
    init();
  }

  @Override
  public void reset() {
    init();
  }
}
