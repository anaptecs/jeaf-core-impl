/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.unmanaged;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 * This is a simple implementation for a UserTransaction and should only be used as long as JEAF does not support
 * transactions in JSE environments.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
final class SimpleUserTransaction implements UserTransaction {
  /**
   * Status of the transaction. The valid values are defined by interface javax.transaction.Status
   */
  private int status;

  /**
   * Initialize object.
   */
  SimpleUserTransaction( ) {
    status = Status.STATUS_NO_TRANSACTION;
  }

  /**
   * Method can be called in order to start a transaction.
   * 
   * @see javax.transaction.UserTransaction#begin()
   */
  public synchronized void begin( ) {
    status = Status.STATUS_ACTIVE;
  }

  /**
   * Method can be called in order to commit a transaction.
   * 
   * @see javax.transaction.UserTransaction#commit()
   */
  public synchronized void commit( ) {
    status = Status.STATUS_COMMITTED;
  }

  /**
   * Method returns the status of a transaction.
   * 
   * 
   * @return int Status of the transaction. The valid values are defined by interface {@link javax.transaction.Status}.
   * 
   * @see javax.transaction.UserTransaction#getStatus()
   */
  public synchronized int getStatus( ) {
    return status;
  }

  /**
   * Method can be called in order to rollback a transaction.
   * 
   * @see javax.transaction.UserTransaction#rollback()
   */
  public synchronized void rollback( ) {
    status = Status.STATUS_ROLLEDBACK;
  }

  /**
   * Method can be called in order to mark a transaction for rollback.
   * 
   * @see javax.transaction.UserTransaction#setRollbackOnly()
   */
  public synchronized void setRollbackOnly( ) {
    status = Status.STATUS_MARKED_ROLLBACK;
  }

  /**
   * Method can be called in order to set the timeout of the transaction.
   * 
   * @param pSeconds Seconds until the transaction times out.
   * 
   * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
   */
  public synchronized void setTransactionTimeout( int pSeconds ) {
    // Nothing to do.
  }
}
