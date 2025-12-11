/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

/**
 * Enumeration defines the possible states of a transaction context.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
enum TxStatus {
  /**
   * No transaction is running and thus no transaction context exists.
   */
  NO_TX_RUNNING,

  /**
   * A transaction is currently running and thus a transaction context exists.
   */
  TX_RUNNING
}