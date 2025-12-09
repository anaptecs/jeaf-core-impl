/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anaptecs.jeaf.core.api.Activity;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.servicechannel.api.ActivityRegistry;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;

/**
 * Class implements an activity registry for JEAF.
 * 
 * @author JEAF Development Team
 * @version JEAF Release 1.3
 */
public class ActivityRegistryImpl implements ActivityRegistry {
  /**
   * Map contains all available activities.
   */
  private final Map<Class<? extends Activity>, Activity> activities = new HashMap<>();

  /**
   * Ensure that there is only one instance.
   */
  public ActivityRegistryImpl( ) {
    // Nothing to do
  }

  /**
   * Method returns the activity of the passed type.
   * 
   * @param pActivityClass Class object representing the requested activity. The parameter must not be null.
   * @return {@link Activity} Requested activity. The method returns never null.
   * @throws JEAFSystemException in case that no activity of the requested type could be found.
   */
  @Override
  public Activity getActivity( Class<? extends Activity> pActivityClass ) throws JEAFSystemException {
    // Check parameter.
    Check.checkInvalidParameterNull(pActivityClass, "pActivityClass");

    // Lookup activity
    Activity lActivity = activities.get(pActivityClass);
    if (lActivity != null) {
      // Return requested activity
      return lActivity;
    }
    // Activity is unknown.
    else {
      throw new JEAFSystemException(MessageConstants.ACTIVITY_NOT_AVAILABLE, pActivityClass.getName());
    }
  }

  /**
   * Method adds the passed activity to the registry. Exiting implementations of an activity must not be overridden.
   * 
   * @param pActivityClass Interface of the activity that should be added. The parameter must not be null.
   * @param pActivityImpl Activity implementation that should be added. The parameter must not be null.
   * @throws JEAFSystemException in case that someone tries to override an existing activity implementation.
   */
  @Override
  public void registerActivity( Class<? extends Activity> pActivityClass, Activity pActivityImpl )
    throws JEAFSystemException {
    // Check parameters
    Check.checkInvalidParameterNull(pActivityClass, "pActivityClass");
    Check.checkInvalidParameterNull(pActivityImpl, "pActivityImpl");

    // Check if an implementation of the activity is already set.
    if (activities.containsKey(pActivityClass) == false) {
      activities.put(pActivityClass, pActivityImpl);
    }
    // There is already an implementation for the activity.
    else {
      throw new JEAFSystemException(MessageConstants.ACTIVITY_IMPL_ALREADY_SET, pActivityImpl.getClass().getName(),
          pActivityClass.getName());
    }
  }

  /**
   * Method returns all activities of the registry.
   * 
   * @return {@link List} List with all activities. The method never returns null.
   */
  @Override
  public Collection<Activity> getAllActivities( ) {
    return Collections.unmodifiableCollection(activities.values());
  }
}
