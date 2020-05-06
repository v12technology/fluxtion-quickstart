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
import com.fluxtion.quickstart.roomsensor.generated.Map_collection_With_warningSensors0;
import java.util.Collection;
import java.util.function.Function;

/**
 * generated filter function wrapper.
 *
 * <ul>
 *   <li>input class : {@link Collection}
 *   <li>filter function : {@link Function#apply}
 * </ul>
 *
 * @author Greg Higgins
 */
public class Filter_Collection_By_apply0 extends AbstractFilterWrapper<Collection> {

  //source operand inputs
  public Map_collection_With_warningSensors0 filterSubject;
  public Map_collection_With_warningSensors0 source_0;
  @NoEventReference public Function f;
  @NoEventReference public Object resetNotifier;
  private boolean parentReset = false;

  @Initialise
  public void init() {
    result = false;
  }

  @OnEvent
  @SuppressWarnings("unchecked")
  public boolean onEvent() {
    boolean oldValue = result;
    result = (boolean) f.apply((Object) ((Collection) filterSubject.event()));
    //this is probably right - to be tested
    //return (!notifyOnChangeOnly | !oldValue) & result;
    return (!notifyOnChangeOnly & result) | ((!oldValue) & result);
  }

  @OnParentUpdate("resetNotifier")
  public void resetNotification(Object resetNotifier) {
    parentReset = true;
    if (isResetImmediate()) {
      result = false;
      parentReset = false;
    }
  }

  @AfterEvent
  public void resetAfterEvent() {
    if (parentReset) {
      result = false;
    }
    parentReset = false;
  }

  @Override
  public FilterWrapper<Collection> resetNotifier(Object resetNotifier) {
    this.resetNotifier = resetNotifier;
    return this;
  }

  @Override
  public Collection event() {
    return (Collection) filterSubject.event();
  }

  @Override
  public Class<Collection> eventClass() {
    return Collection.class;
  }
}
