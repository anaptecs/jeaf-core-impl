/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

/**
 * Enumeration defines the possible transitions of a transaction context.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public enum TxContextTransition {
  /**
   * A new transaction context is required.
   */
  NEW_TX_REQUIRED,

  /**
   * A currently running transaction has to be suspended so that there will be no more active transaction context.
   */
  NO_TX_REQUIRED,

  /**
   * The current transaction context has to remain unchanged. If none exists a new transaction context must not be
   * created.
   */
  UNCHANGED,

  /**
   * A invalid transaction context transition is requested and so an error has to be reported.
   */
  ERROR
}