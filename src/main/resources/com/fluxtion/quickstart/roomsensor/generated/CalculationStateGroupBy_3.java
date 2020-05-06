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
 * generated group by calculation state holder. This class holds the state of a group by
 * calculation.
 *
 * <p>target class : SensorReadingDerived
 *
 * @author Greg Higgins
 */
public final class CalculationStateGroupBy_3 implements Wrapper<SensorReadingDerived> {

  private static final int SOURCE_COUNT = 1;
  private final BitSet updateMap = new BitSet(SOURCE_COUNT);

  public SensorReadingDerived target;
  public int aggregateMax1;
  public AggregateAverage aggregateAverage2Function = new AggregateAverage();
  public double aggregateAverage2;

  public CalculationStateGroupBy_3() {
    target = new SensorReadingDerived();
  }

  public boolean allMatched() {
    return SOURCE_COUNT == updateMap.cardinality();
  }

  /**
   * @param index
   * @param initialiser
   * @param source
   * @return The first time this is a complete record is processed
   */
  public boolean processSource(int index, GroupByIniitialiser initialiser, Object source) {
    boolean prevMatched = allMatched();
    if (!updateMap.get(index)) {
      initialiser.apply(source, target);
    }
    updateMap.set(index);
    return allMatched() ^ prevMatched;
  }

  /**
   * @param index
   * @param source
   * @return The first time this is a complete record is processed
   */
  public boolean processSource(int index, Object source) {
    boolean prevMatched = allMatched();
    updateMap.set(index);
    return allMatched() ^ prevMatched;
  }

  @Override
  public SensorReadingDerived event() {
    return target;
  }

  @Override
  public Class<SensorReadingDerived> eventClass() {
    return SensorReadingDerived.class;
  }

  @Override
  public String toString() {
    return event().toString();
  }
}
