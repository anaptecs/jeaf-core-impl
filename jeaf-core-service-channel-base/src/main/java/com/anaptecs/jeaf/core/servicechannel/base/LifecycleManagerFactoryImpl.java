/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManagerFactory;

@com.anaptecs.jeaf.core.annotations.LifecycleManagerFactory
public class LifecycleManagerFactoryImpl implements LifecycleManagerFactory {
  @Override
  public LifecycleManager createLifecycleManager( ) {
    // Create new instance of life cycle manager implementation.
    return CoreConfiguration.getInstance().getLifecycleManager();
  }
}
