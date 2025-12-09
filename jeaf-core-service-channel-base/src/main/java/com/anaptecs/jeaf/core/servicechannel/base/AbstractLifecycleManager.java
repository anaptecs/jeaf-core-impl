/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import java.util.HashSet;
import java.util.Set;

import com.anaptecs.jeaf.core.api.Context;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.servicechannel.api.ContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleListener;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * The life cycle managers task is to perform all life cycle operations of JEAF. Since these actions depend very much on
 * the environment (like EJB Containers, EJB Client Container or pure J2SE environments) implementations for each
 * environment have to be implemented. Which concrete implementation will be used can be configured in the JEAF
 * properties.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public abstract class AbstractLifecycleManager implements LifecycleManager {
  /**
   * Enum defines the states that a lifecycle manager may have.
   */
  private enum LifecycleManagerState {
    NOT_INITIALIZED, INITIALIZED, SHUTDOWN;
  }

  /**
   * Attribute describes the state of this class. The attribute may have the values NOT_STARTED, STARTED or SHUTDOWN.
   */
  private LifecycleManagerState state;

  /**
   * Set contains all objects that are interested in life cycle events. The set is never null since an empty instance
   * will be created in the class' constructor.
   */
  private Set<LifecycleListener> lifecycleListeners;

  /**
   * Reference to session context manager.
   */
  private final ContextManager contextManager;

  /**
   * Initialize object. Thereby the object will be set its initial state NOT_STARTED.
   */
  protected AbstractLifecycleManager( ) {
    state = LifecycleManagerState.NOT_INITIALIZED;
    lifecycleListeners = new HashSet<LifecycleListener>();
    CoreConfiguration lConfiguration = CoreConfiguration.getInstance();
    contextManager = lConfiguration.getContextManager();
  }

  /**
   * Method adds the passed object to the list of life cycle listeners. The objects will be informed about JEAF's life
   * cycle events. This will only happen if the passed object implements the interface LifecycleListener. This method is
   * intended for internal usage only. For this reason any object can be passed to this method. Whenever the passed
   * object implements the interface LifecycleManager it will be registered as listener.
   * 
   * @param pLifecycleListener Object that should be added as life cycle listener. The parameter must not be null.
   */
  public final void addLifecycleListener( LifecycleListener pLifecycleListener ) {
    // Check parameter for null.
    Assert.assertNotNull(pLifecycleListener, "pLifecycleListener");

    // Add life cycle listener.
    lifecycleListeners.add(pLifecycleListener);
  }

  /**
   * Method triggers the initialization of all parts of JEAF and the components and services based on the JEAF
   * Framework. The method must not be called if this object is not in state NOT_INITIALIZED.
   */
  public final void initialize( ) {
    // Check state.
    if (state == LifecycleManagerState.NOT_INITIALIZED) {
      // Trace message
      Trace lTrace = XFun.getTrace();
      long startTime = System.currentTimeMillis();
      String lLifeCycleManagerName = this.getClass().getSimpleName();

      // Execute environment specific initializations.
      this.performStartup();

      // Set state.
      state = LifecycleManagerState.INITIALIZED;

      // Trace message
      String lExecutionTime = Long.toString(System.currentTimeMillis() - startTime);
      lTrace.write(MessageConstants.COMPLETED_LIFECYCLE_MANAGER_INIT, lLifeCycleManagerName, lExecutionTime);
    }
    // JEAF is already initialized.
    else {
      // Nothing to do.
    }
  }

  /**
   * Method checks whether this instance of a life cycle manager has already been initialized.
   * 
   * @return boolean The method returns true if the life cycle manager already has been initialized or false in all
   * other cases.
   */
  public final synchronized boolean isInitalized( ) {
    return state == LifecycleManagerState.INITIALIZED;
  }

  /**
   * Method triggers the shutdown of all parts of JEAF and the components and services based on the JEAF Framework. The
   * method must not be called of this object is not in state INITIALIZED.
   */
  public final synchronized void shutdown( ) {
    // Check state.
    if (state == LifecycleManagerState.INITIALIZED) {
      // Perform implementation specific shutdown actions.
      this.performShutdown();

      // Set state.
      state = LifecycleManagerState.SHUTDOWN;
    }
    // Lifecycle manager is not in state INITIALIZED
    else {
      String[] lParams = new String[] { state.name() };
      throw new JEAFSystemException(MessageConstants.UNABLE_TO_SHUTDOWN_JEAF, lParams);
    }
  }

  /**
   * Method returns a representation of the context. The context object can be used to access the different types of
   * contexts.
   * 
   * @return {@link Context} representation of the context. The method never returns null.
   */
  @Override
  public Context getContext( ) {
    return contextManager.getContext();
  }

  /**
   * Method returns the context manager that is configured.
   * 
   * @return {@link ContextManager} Current context manager. The method never returns null.
   */
  @Override
  public ContextManager getContextManager( ) {
    return contextManager;
  }

  /**
   * Method injects all dependencies to JEAF services and service providers to the passed object. Therefore the method
   * checks all fields of the class and all of its parents classes for annotated fields that declare a dependency to a
   * JEAF Service or Service Provider.
   * 
   * The method must not be called from inside of service or service provider implementations as this may lead to
   * problems during bootstrapping. For these classes the dependency injection will be done automatically.
   * 
   * @param pObject Object on which the dependencies should be injected. The parameter must not be null and the passed
   * object will be modified.
   */
  @Override
  public void injectDependencies( Object pObject ) {
    DependencyInjector lDependencyInjector = new DependencyInjector(this);
    lDependencyInjector.injectJEAFDependencies(pObject);
  }

  /**
   * Method is called on JEAF's initialization in order to load required components and service. Which actions have to
   * be performed depends on the concrete environment.
   */
  protected abstract void performStartup( );

  /**
   * Method is called on JEAF's shutdown in order to provide a shutdown hook for specific life cycle manager
   * implementations. Which actions are performed by this method depends on the concrete environment for which the life
   * cycle manager implementation is designed.
   */
  protected abstract void performShutdown( );
}
