package com.fluxtion.quickstart.roomsensor.generated;

import com.fluxtion.api.annotations.AfterEvent;
import com.fluxtion.api.annotations.Initialise;
import com.fluxtion.api.annotations.NoEventReference;
import com.fluxtion.api.annotations.OnEvent;
import com.fluxtion.api.annotations.OnParentUpdate;
import com.fluxtion.api.annotations.PushReference;
import com.fluxtion.ext.streaming.api.FilterWrapper;
import com.fluxtion.ext.streaming.api.Test;
import com.fluxtion.ext.streaming.api.Wrapper;
import com.fluxtion.ext.streaming.api.stream.AbstractFilterWrapper;
import com.fluxtion.quickstart.roomsensor.SensorMonitor.TempertureController;
import com.fluxtion.quickstart.roomsensor.generated.Filter_Collection_By_apply0;
import java.util.Collection;

/**
 * Generated push function wrapper.
 *
 * <ul>
 *   <li>input class : {@link Collection}
 *   <li>push target : {@link TempertureController#investigateSensors}
 * </ul>
 *
 * @author Greg Higgins
 */
public class Push_Collection_To_investigateSensors0
    implements Wrapper<Push_Collection_To_investigateSensors0> {

  public Filter_Collection_By_apply0 filterSubject;
  @PushReference public TempertureController f;

  @OnEvent
  public boolean onEvent() {
    f.investigateSensors((Collection) ((Collection) filterSubject.event()));
    return true;
  }

  @Override
  public Push_Collection_To_investigateSensors0 event() {
    return this;
  }

  @Override
  public Class<Push_Collection_To_investigateSensors0> eventClass() {
    return Push_Collection_To_investigateSensors0.class;
  }
}
