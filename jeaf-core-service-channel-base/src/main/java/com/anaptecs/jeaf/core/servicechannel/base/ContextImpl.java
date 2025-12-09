/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.Context;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.core.api.TxContext;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceInvocationContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.SessionContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.TransactionContextManager;
import com.anaptecs.jeaf.xfun.api.checks.Check;

public class ContextImpl implements Context {
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

  public ContextImpl( SessionContextManager pSessionContextManager,
      TransactionContextManager pTransactionContextManager,
      ServiceInvocationContextManager pServiceInvocationContextManager ) {

    // Check parameters
    Check.checkInvalidParameterNull(pSessionContextManager, "pSessionContextManager");
    Check.checkInvalidParameterNull(pTransactionContextManager, "pTransactionContextManager");
    Check.checkInvalidParameterNull(pServiceInvocationContextManager, "pServiceInvocationContextManager");

    // Lookup implementation from configuration.
    sessionContextManager = pSessionContextManager;
    transactionContextManager = pTransactionContextManager;
    serviceInvocationContextManager = pServiceInvocationContextManager;
  }

  /**
   * Method returns the current session context.
   * 
   * @return {@link SessionContext} Current session context or null if none is available.
   */
  @Override
  public SessionContext getSessionContext( ) {
    return sessionContextManager.getSessionContext();
  }

  /**
   * Method checks if a session context is available.
   * 
   * @return boolean Method returns true if a session context is available and otherwise false.
   */
  @Override
  public boolean isSessionContextAvailable( ) {
    return sessionContextManager.isSessionContextAvailable();
  }

  /**
   * Method returns the current transaction context
   * 
   * @return {@link TxContext} Current transaction context or null fi none is available.
   */
  @Override
  public TxContext getTransactionContext( ) {
    return transactionContextManager.getCurrentTransactionContext();
  }

  /**
   * Method checks if a transaction context is available.
   * 
   * @return boolean Method returns true if a transaction context is available and otherwise false.
   */
  @Override
  public boolean isTransactionContextAvailable( ) {
    return transactionContextManager.isTransactionContextAvailable();
  }

  /**
   * Method returns current service invocation context.
   * 
   * @return {@link ServiceInvocationContext} Current service invocation context or null none is available.
   */
  @Override
  public ServiceInvocationContext getServiceInvocationContext( ) {
    return serviceInvocationContextManager.getCurrentServiceInvocationContext();
  }

  /**
   * Method checks if a service invocation context is available.
   * 
   * @return boolean Method returns true if a service invocation context is available and otherwise false.
   */
  @Override
  public boolean isServiceInvocationContextAvailable( ) {
    return serviceInvocationContextManager.isServiceInvocationContextAvailable();
  }

  /**
   * Method returns the current component.
   * 
   * @return {@link Component} Component in whose context we are currently running. If the method is not called from
   * within a service call then the method will return null.
   */
  @Override
  public Component getCurrentComponent( ) {
    return this.getServiceInvocationContext().getComponent();
  }

  /**
   * Method returns the current component. By providing the wished type clients can avoid down casting to the wished
   * type.
   * 
   * @return {@link Component} Component in whose context we are currently running. If the method is not called from
   * within a service call then the method will return null.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T extends Component> T getCurrentComponent( Class<T> pComponentType ) {
    return (T) this.getCurrentComponent();
  }
}
