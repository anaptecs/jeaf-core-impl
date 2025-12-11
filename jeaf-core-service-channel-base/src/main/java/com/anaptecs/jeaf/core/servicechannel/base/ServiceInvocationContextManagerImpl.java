/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceInvocationContextManager;

public class ServiceInvocationContextManagerImpl implements ServiceInvocationContextManager {
  /**
   * Thread local attribute contains the context for the current thread.
   */
  private static ThreadLocal<ServiceInvocationContext> currentServiceInvocationContext =
      new ThreadLocal<ServiceInvocationContext>();

  @Override
  public ServiceInvocationContext getCurrentServiceInvocationContext( ) {
    return currentServiceInvocationContext.get();
  }

  @Override
  public void setToCurrentServiceInvocationContext( ServiceInvocationContext pServiceInvocationContext ) {
    // Ensure that this context object is still valid.
    pServiceInvocationContext.checkValidity();

    // Set this object to the current context.
    currentServiceInvocationContext.set(pServiceInvocationContext);
  }

  @Override
  public void unsetAsCurrentServiceInvocationContext( ServiceInvocationContext pServiceInvocationContext ) {
    // Ensure that this context object is still valid.
    pServiceInvocationContext.checkValidity();

    // TODO Check if this object is really the current context.

    // Set current context to null.
    currentServiceInvocationContext.set(null);
  }

  @Override
  public boolean isServiceInvocationContextAvailable( ) {
    return this.getCurrentServiceInvocationContext() != null;
  }
}
