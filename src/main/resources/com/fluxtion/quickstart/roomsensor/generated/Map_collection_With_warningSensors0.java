package com.fluxtion.quickstart.roomsensor.generated;

import com.fluxtion.api.annotations.AfterEvent;
import com.fluxtion.api.annotations.Initialise;
import com.fluxtion.api.annotations.NoEventReference;
import com.fluxtion.api.annotations.OnEvent;
import com.fluxtion.api.annotations.OnParentUpdate;
import com.fluxtion.ext.streaming.api.FilterWrapper;
import com.fluxtion.ext.streaming.api.Test;
import com.fluxtion.ext.streaming.api.Wrapper;
import com.fluxtion.ext.streaming.api.stream.AbstractFilterWrapper;
import com.fluxtion.ext.streaming.api.test.BooleanFilter;
import com.fluxtion.quickstart.roomsensor.SensorMonitor;
import com.fluxtion.quickstart.roomsensor.generated.GroupBy_3;
import java.util.Collection;

/**
 * Generated mapper function wrapper for a reference type.
 *
 * <ul>
 *   <li>output class : {@link Collection}
 *   <li>input class : {@link GroupBy_3}
 *   <li>map function : {@link SensorMonitor#warningSensors}
 * </ul>
 *
 * @author Greg Higgins
 */
public class Map_collection_With_warningSensors0 extends AbstractFilterWrapper<Collection> {

  public BooleanFilter filterSubject;
  private Collection result;

  @OnEvent
  public boolean onEvent() {
    Collection oldValue = result;
    result =
        SensorMonitor.warningSensors((Collection) ((GroupBy_3) filterSubject.event()).collection());
    return !notifyOnChangeOnly || (!result.equals(oldValue));
  }

  @Override
  public Collection event() {
    return result;
  }

  @Override
  public Class<Collection> eventClass() {
    return Collection.class;
  }

  @Initialise
  public void init() {
    result = null;
  }
}
