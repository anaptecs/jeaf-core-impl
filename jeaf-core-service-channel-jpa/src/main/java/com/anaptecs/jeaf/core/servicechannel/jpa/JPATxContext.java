/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */

package com.anaptecs.jeaf.core.servicechannel.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.TxContext;
import com.anaptecs.jeaf.core.spi.ComponentImplementation;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;

/**
 * Class provides all functions that are required to have an JPA enabled transaction context. Since there are slight
 * differences between the use of JPA within an EJB container or in a unmanaged environment this class is abstract.
 * Concrete subclasses have to deal with the slight difference of looking up an entity manager
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public abstract class JPATxContext extends TxContext {
  /**
   * Default serial version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Map contains all entity managers that were used within this transaction context. Every time a entity manager is
   * required for a component a lookup will be performed and the result will be stored within this map until the context
   * is invalidated.
   */
  private final Map<String, EntityManager> entityManagers = new HashMap<String, EntityManager>();

  /**
   * Map contains all entity manager factories that were created by this object.
   */
  private static final Map<String, EntityManagerFactory> FACTORIES = new HashMap<String, EntityManagerFactory>();

  /**
   * Initialize object.
   */
  public JPATxContext( ) {
    // Nothing to do.
  }

  /**
   * Method returns the entity manager for the component of the current service call that is used within the transaction
   * that is represented by this transaction context.
   * 
   * @return {@link EntityManager} Used entity manager. The method never returns null.
   */
  public final EntityManager getCurrentEntityManager( ) {
    // Get component of current service call.
    ServiceInvocationContext lServiceInvocationContext = JEAF.getContext().getServiceInvocationContext();
    ComponentImplementation lComponent = (ComponentImplementation) lServiceInvocationContext.getComponent();

    // Check if a lookup for the entity manager of the current component was already performed.
    String lPersistenceUnitName = lComponent.getPersistenceUnitName();
    EntityManager lCurrentEntityManager = entityManagers.get(lPersistenceUnitName);

    // Entity manager was not used before.
    if (lCurrentEntityManager == null) {
      // Perform lookup for entity manager and store it within the map of entity managers.
      lCurrentEntityManager = this.lookupEntityManager(lComponent);
      entityManagers.put(lPersistenceUnitName, lCurrentEntityManager);
    }
    return lCurrentEntityManager;
  }

  /**
   * Method returns all entity managers that were used within this transaction context.
   * 
   * @return {@link Collection} List with all entity managers that were used within this transaction context. The method
   * never returns null.
   */
  public final Collection<EntityManager> getAllEntityManagers( ) {
    return entityManagers.values();
  }

  /**
   * Method performs a lookup for the entity manager for the passed component. This method is based on the design that
   * every JEAF Component has its own entity manager. The since the way how to lookup an entity manager is highly
   * platform dependent the implementation has to be provided by concrete subclasses. This class ensures by caching
   * 
   * @param pComponent Component whose entity manager should be returned by this method. The parameter is never null.
   * @return {@link EntityManager} Entity manager for the passed component. The method must not return null.
   */
  protected abstract EntityManager lookupEntityManager( ComponentImplementation pComponent );

  /**
   * Method returns the cached entity manager factory that was cached for the persistence unit with the passed name.
   * 
   * @param pPersistenceUnitName Name of the persistence unit whose entity manager factory should be returned. The
   * parameter must not be null.
   * @return {@link EntityManagerFactory} Factory that belongs to the persistence unit with the passed name. The method
   * may return null.
   */
  public static EntityManagerFactory getCachedEntityManagerFactory( String pPersistenceUnitName ) {
    // Check parameters.
    Assert.assertNotNull(pPersistenceUnitName, "pPersistenceUnitName");

    return FACTORIES.get(pPersistenceUnitName);
  }

  /**
   * Method adds the passed entity manager factory to the cache.
   * 
   * @param pPersistenceUnitName Name of the persistence unit to which the entity manager factory belongs to. The
   * parameter must not be null.
   * @param pEntityManagerFactory Entity manager factory that should be cached. The parameter must not be null.
   */
  protected final void registerEntityManagerFactory( String pPersistenceUnitName,
      EntityManagerFactory pEntityManagerFactory ) {

    // Check parameters.
    Assert.assertNotNull(pPersistenceUnitName, "pPersistenceUnitName");
    Assert.assertNotNull(pEntityManagerFactory, "pEntityManagerFactory");

    // Add factory to static map with all factories.
    FACTORIES.put(pPersistenceUnitName, pEntityManagerFactory);
  }

  public static void registerEntityManagerFactories( Map<String, EntityManagerFactory> pFactories ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pFactories, "pFactories");

    // Add all factories
    for (Entry<String, EntityManagerFactory> lNextEntry : pFactories.entrySet()) {
      FACTORIES.put(lNextEntry.getKey(), lNextEntry.getValue());
    }
  }
}
