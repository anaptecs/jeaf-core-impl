/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.api.TxContext;
import com.anaptecs.jeaf.core.servicechannel.api.TransactionContextManager;

public class TransactionContextManagerImpl implements TransactionContextManager {
  /**
   * Thread local attribute contains the context for the current thread.
   */
  private static ThreadLocal<TxContext> currentTxContext = new ThreadLocal<TxContext>();

  /**
   * Method returns the current transaction context.
   * 
   * @return {@link TxContext} Current transaction context or null if none is running.
   */
  @Override
  public TxContext getCurrentTransactionContext( ) {
    return currentTxContext.get();
  }

  /**
   * Method sets this TxContext object to the current context.
   */
  @Override
  public void setToCurrentTxContext( TxContext pTxContext ) {
    // Ensure that this context object is still valid.
    pTxContext.checkValidity();

    // Set this object to the current context.
    currentTxContext.set(pTxContext);
  }

  /**
   * Method removes this context object as current.
   */
  @Override
  public void unsetAsCurrentTxContext( TxContext pTxContext ) {
    // Ensure that this context object is still valid.
    pTxContext.checkValidity();

    // TODO Check if this object is really the current context.

    // Set current context to null.
    currentTxContext.set(null);
  }

  @Override
  public boolean isTransactionContextAvailable( ) {
    return this.getCurrentTransactionContext() != null;
  }
}
