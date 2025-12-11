/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.unmanaged;

import javax.transaction.UserTransaction;

import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.base.GenericLifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.base.SchedulingConfiguration;

/**
 * This class implements a JEAF lifecycle manager. This implementation does not contain any environment specific
 * operations and therefore can be used in multiple situations. During initialization all components and services will
 * be created / loaded.
 * 
 * @author JEAF Development Team
 */
public class LocalLifecycleManagerImpl extends GenericLifecycleManager {
  /**
   * Timer object that is used by this lifecycle manager.
   */
  private final TimerImpl timer;

  /**
   * Initialize object.
   */
  public LocalLifecycleManagerImpl( ) {
    // Create JSE specific timer.
    timer = new TimerImpl(this);
  }

  /**
   * Method creates a new instance of the service channel that should be used in the specific runtime environment for
   * which a concrete life cycle manager implementation is designed for. In this case a local service channel
   * implementation will be returned.
   * 
   * @return ServiceChannel Service channel implementation that should be used in this specific environment. The method
   * never returns null.
   */
  @Override
  protected ServiceChannel createServiceChannel( ) {
    // Create new core service channel and return it.
    return new LocalServiceChannelImpl(this);
  }

  /**
   * Method returns a UserTransaction object that can be used by the lifecycle manager for internal use only.
   * 
   * @return {@link UserTransaction} UserTransaction that can be used by the lifecycle manager. The method never returns
   * null.
   */
  @Override
  protected UserTransaction getUserTransaction( ) {
    return new SimpleUserTransaction();
  }

  /**
   * Method overrides implementation of base class in order to provide platform specific triggering mechanism. This
   * means for JSE environments that JEAF that a timer task will be used.
   */
  @Override
  public void performStartup( ) {
    // Call implementation of base class.
    super.performStartup();

    // Initialize trigger mechanism if it is enabled
    if (SchedulingConfiguration.getInstance().isJEAFSchedulingEnabled() == true) {
      final int lInterval = this.getTriggerInterval();
      timer.start(lInterval, lInterval);
    }
  }

  /**
   * Method overrides implementation of base class in order to stop the started timer again.
   */
  @Override
  public void performShutdown( ) {
    // Call implementation of base class.
    super.performShutdown();

    timer.stop();
  }
}