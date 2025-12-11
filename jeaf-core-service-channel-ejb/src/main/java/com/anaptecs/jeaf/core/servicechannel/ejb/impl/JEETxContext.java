/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.servicechannel.jpa.JPATxContext;
import com.anaptecs.jeaf.core.spi.ComponentImplementation;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;

/**
 * Class provides an JPA Transaction Context implementation for the use within JEE environments.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public final class JEETxContext extends JPATxContext {
  /**
   * Default Serial Version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Reference to EJB session context. This object is used to tell the EJB container that an roll back of the current
   * transaction is required.
   * 
   * @see #setRollbackOnly()
   * @see #getRollbackOnly()
   */
  private final transient SessionContext sessionContext;

  /**
   * Initialize object.
   * 
   * @param pSessionContext Session context for the current transaction. The parameter must not be null.
   */
  JEETxContext( SessionContext pSessionContext ) {
    // Check parameter for null.
    Assert.assertNotNull(pSessionContext, "pSessionContext");

    sessionContext = pSessionContext;
  }

  /**
   * Method performs a lookup for the entity manager for the passed component. This method is based on the design that
   * every JEAF Component has its own entity manager. In this implementation the entity manager is created in the way as
   * it is defined by JPA for JEE environments.
   * 
   * @param pComponent Component whose entity manager should be returned by this method. The parameter is never null.
   * @return {@link EntityManager} Entity manager for the passed component. The method must not return null.
   * 
   * @see JPATxContext#lookupEntityManager(Component)
   */
  @Override
  protected EntityManager lookupEntityManager( ComponentImplementation pComponent ) {
    // Check parameter for null.
    Assert.assertNotNull(pComponent, "pComponent");

    // Get entity manager factory and create new entity manager for the passed component.
    return this.lookupEntityManagerFactory(pComponent).createEntityManager();
  }

  /**
   * Method performs a lookup for the entity manager factory for the passed component. This method is based on the
   * design that every JEAF Component may have its own entity manager.
   * 
   * @param pComponent Component whose entity manager should be returned by this method. The parameter is never null.
   * @return {@link EntityManagerFactory} Entity manager factory for the passed component. The method must not return
   * null.
   */
  private EntityManagerFactory lookupEntityManagerFactory( ComponentImplementation pComponent ) {
    // Check parameter for null.
    Assert.assertNotNull(pComponent, "pComponent");

    // Try to get cached factory or create it if it does not exist yet.
    final String lPersistenceUnitName = pComponent.getPersistenceUnitName();
    EntityManagerFactory lFactory = JPATxContext.getCachedEntityManagerFactory(lPersistenceUnitName);

    // Factory was not yet used.
    if (lFactory == null) {
      // Get JNDI name of entity manager factory.
      EJBContainerConfiguration lConfiguration = new EJBContainerConfiguration();
      String lEntityManagerJNDIRootPath = lConfiguration.getEntityManagerJNDIRootPath();
      String lEntityManagerJNDI;
      if (lEntityManagerJNDIRootPath != null) {
        lEntityManagerJNDI = lEntityManagerJNDIRootPath + "/" + pComponent.getPersistenceUnitName();
      }
      else {
        lEntityManagerJNDI = pComponent.getPersistenceUnitName();
      }

      // Use JNDI lookup to find entity manager factory
      try {
        XFun.getTrace().write(MessageConstants.LOOKING_UP_ENTITY_MANAGER, new String[] { lEntityManagerJNDI });

        // Create new initial context for JNDI lookups.
        InitialContext lInitialContext = new InitialContext();
        // Lookup entity manager and return it.
        lFactory = (EntityManagerFactory) lInitialContext.lookup(lEntityManagerJNDI);
      }
      // Catch Naming exception if the entity manager factory could not be found.
      catch (NamingException e) {
        throw new JEAFSystemException(MessageConstants.LOOKUP_FOR_ENTITY_MANAGER_FAILED, e, lEntityManagerJNDI);
      }

      // Add factory to static map with all factories.
      this.registerEntityManagerFactory(lPersistenceUnitName, lFactory);
    }

    // Return entity manager factory.
    return lFactory;
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
    // Determine transaction status using EJB session context.
    return sessionContext.getRollbackOnly();
  }

  /**
   * Method marks the transaction that is represented by this transaction context for roll back only. This means that
   * the transaction can no longer be committed.
   * 
   * @see com.anaptecs.jeaf.core.api.TxContext#setRollbackOnly()
   */
  @Override
  public void setRollbackOnly( ) {
    // Delegate call to EJB container via the session context.
    sessionContext.setRollbackOnly();
  }
}