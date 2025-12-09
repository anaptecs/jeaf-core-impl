/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */

package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.base.GenericLifecycleManager;

/**
 * This class implements a JEAF life cycle manager that is intended to be used when JEAF is running inside an EJB
 * container.
 * 
 * @author Tillmann Schall (tls)
 * @version 1.0
 */
public final class EJBLifecycleManager extends GenericLifecycleManager {
  /**
   * Constant for the JNDI name of the user transaction object as defined by the JEE standard.
   */
  public static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

  /**
   * Initialize object.
   */
  public EJBLifecycleManager( ) {
    // Nothing to do.
  }

  /**
   * Method creates a new instance of the service channel that should be used in the specific runtime environment for
   * which a concrete life cycle manager implementation is designed for.
   * 
   * @return ServiceChannel Service channel implementation that should be used in this specific environment. The method
   * must not return null.
   * 
   * @see com.anaptecs.jeaf.core.servicechannel.base.GenericLifecycleManager#createServiceChannel()
   */
  @Override
  protected ServiceChannel createServiceChannel( ) {
    // Return new instance of EJB local service channel implementation.
    return new EJBLocalServiceChannel();
  }

  /**
   * Method returns a UserTransaction object that can be used by the lifecycle manager for internal use only.
   * 
   * @return {@link UserTransaction} UserTransaction that can be used by the lifecycle manager. The method never returns
   * null.
   * 
   * @throws NamingException If the JNDI lookup for the UserTransaction object fails.
   */
  @Override
  protected UserTransaction getUserTransaction( ) throws NamingException {
    Context lContext = new InitialContext();
    return (UserTransaction) lContext.lookup(USER_TRANSACTION_JNDI_NAME);
  }
}
