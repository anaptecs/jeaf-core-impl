/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.unmanaged;

import com.anaptecs.jeaf.core.api.TxContext;

/**
 * This is a really simple implementation of a transaction context and can not be used as an implementation for
 * production use if real transaction handling is required.
 * 
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
final class SimpleTxContext extends TxContext {
  /**
   * Default Serial Version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Attribute indicates if an roll back is required for te represented transaction.
   */
  private boolean txRollbackRequired;

  /**
   * Initialize object.
   */
  SimpleTxContext( ) {
    txRollbackRequired = false;
  }

  /**
   * Method checks whether the transaction represented by this transaction context is marked for roll back only.
   * 
   * @return boolean The method returns true if the transaction is marked for roll back only. and false in all other
   * cases.
   * 
   * @see com.anaptecs.jeaf.core.api.TxContext#getRollbackOnly()
   */
  @Override
  public boolean getRollbackOnly( ) {
    return txRollbackRequired;
  }

  /**
   * Method enables objects that are called within the context of a transaction to request a roll back of the currently
   * running transaction. Once a roll back was requested it can not be canceled anymore and all actions performed within
   * the current transaction will not be committed. The method can be called more than once within an transaction.
   * 
   * @see TxContext#setRollbackOnly()
   */
  @Override
  public void setRollbackOnly( ) {
    txRollbackRequired = true;
  }
}
