/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.api.Context;
import com.anaptecs.jeaf.core.servicechannel.api.ContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceInvocationContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.SessionContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.TransactionContextManager;

public class ContextManagerImpl implements ContextManager {
  /**
   * Reference to configured session context manager. Which implementation will be used depends on the configuration.
   */
  private final SessionContextManager sessionContextManager;

  /**
   * Reference to configured transaction context manager. Which implementation will be used depends on the
   * configuration.
   */
  private final TransactionContextManager transactionContextManager;

  /**
   * Reference to configured service invocation context manager. Which implementation will be used depends on the
   * configuration.
   */
  private final ServiceInvocationContextManager serviceInvocationContextManager;

  /**
   * Reference to object representing the context.
   */
  private final Context context;

  public ContextManagerImpl( ) {
    // Lookup implementation from configuration.
    CoreConfiguration lConfiguration = CoreConfiguration.getInstance();
    sessionContextManager = lConfiguration.getSessionContextManager();
    transactionContextManager = lConfiguration.getTransactionContextManager();
    serviceInvocationContextManager = lConfiguration.getServiceInvocationContextManager();
    context = new ContextImpl(sessionContextManager, transactionContextManager, serviceInvocationContextManager);
  }

  /**
   * Method returns an object representing the context.
   * 
   * @return {@link Context} Representation of the context. The method never returns null.
   */
  @Override
  public Context getContext( ) {
    return context;
  }

  /**
   * Method returns the {@link SessionContextManager}. Which implementation of the interface will be used depends on the
   * configuration.
   * 
   * @return {@link SessionContextManager} Configured session context manager. The method never returns null.
   */
  @Override
  public SessionContextManager getSessionContextManager( ) {
    return sessionContextManager;
  }

  /**
   * Method returns the {@link TransactionContextManager}. Which implementation of the interface will be used depends on
   * the configuration.
   * 
   * @return {@link SessionContextManager} Configured transaction context manager. The method never returns null.
   */
  @Override
  public TransactionContextManager getTransactionContextManager( ) {
    return transactionContextManager;
  }

  /**
   * Method returns the {@link ServiceInvocationContextManager}. Which implementation of the interface will be used
   * depends on the configuration.
   * 
   * @return {@link ServiceInvocationContextManager} Configured transaction context manager. The method never returns
   * null.
   */
  @Override
  public ServiceInvocationContextManager getServiceInvocationContextManager( ) {
    return serviceInvocationContextManager;
  }
}
