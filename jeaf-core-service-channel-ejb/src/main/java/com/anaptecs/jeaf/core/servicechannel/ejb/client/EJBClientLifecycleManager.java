/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.client;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceProvider;
import com.anaptecs.jeaf.core.jee.commons.EJBHelper;
import com.anaptecs.jeaf.core.servicechannel.api.ActivityRegistry;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProviderRegistry;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProxy;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceRegistry;
import com.anaptecs.jeaf.core.servicechannel.base.AbstractLifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.base.ActivityRegistryImpl;
import com.anaptecs.jeaf.core.servicechannel.base.ProxyOnlyServiceRegistryImpl;
import com.anaptecs.jeaf.core.servicechannel.base.ServiceProviderRegistryImpl;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJB;
import com.anaptecs.jeaf.core.spi.ComponentImplementation;
import com.anaptecs.jeaf.core.spi.ServiceImplementation;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;
import com.anaptecs.jeaf.xfun.api.messages.MessageRepository;

/**
 * Class implements a lifecycle manager for EJB clients. When the lifecycle manager is initialized it contacts the
 * LifcycleManagerEJB to load all available services.
 */
public final class EJBClientLifecycleManager extends AbstractLifecycleManager {
  /**
   * Reference to service channel implementation that is used for EJB clients. The reference is never null since the
   * object is created within the class' constructor.
   */
  private final ServiceChannel serviceChannel;

  /**
   * Reference to service registry that contains all available remote services. The reference is never null since the
   * object is created within the class' constructor.
   */
  private final ProxyOnlyServiceRegistryImpl serviceRegistry;

  /**
   * Reference to service provider registry. As this lifecycle manager is intended to be used on EJB client side it is
   * always empty.
   */
  private final ServiceProviderRegistry serviceProviderRegistry;

  /**
   * Reference to activity registry. As this lifecycle manager is intended to be used on EJB client side it is always
   * empty.
   */
  private final ActivityRegistry activityRegistry;

  /**
   * JNDI name of the remote interface of the remote lifecycle manager.
   */
  private final String lifecycleManagerJNDI;

  /**
   * Initialize object.
   */
  public EJBClientLifecycleManager( ) {
    // Create new service channel and registry.
    EJBClientServiceChannelConfiguration lConfiguration = new EJBClientServiceChannelConfiguration();
    lifecycleManagerJNDI = lConfiguration.getLifecycleManagerJNDI();
    String lServiceChannelJNDI = lConfiguration.getServiceChannelJNDI();

    serviceChannel = new RemoteServiceChannel(lServiceChannelJNDI);
    serviceRegistry = new ProxyOnlyServiceRegistryImpl();
    serviceProviderRegistry = new ServiceProviderRegistryImpl();
    activityRegistry = new ActivityRegistryImpl();
  }

  /**
   * Initialize object.
   * 
   * @param pLifecycleManagerJNDI JNDI name of the lifecycle manager EJB. The parameter must not be null.
   * @param pServiceChannelJNDI JNDI name of the service channel EJB. The parameter must not be null.
   */
  public EJBClientLifecycleManager( String pLifecycleManagerJNDI, String pServiceChannelJNDI ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pLifecycleManagerJNDI, "pLifecycleManagerJNDI");
    Check.checkInvalidParameterNull(pServiceChannelJNDI, "pServiceChannelJNDI");

    // Create new service channel and registry.
    lifecycleManagerJNDI = pLifecycleManagerJNDI;

    serviceChannel = new RemoteServiceChannel(pServiceChannelJNDI);
    serviceRegistry = new ProxyOnlyServiceRegistryImpl();
    serviceProviderRegistry = new ServiceProviderRegistryImpl();
    activityRegistry = new ActivityRegistryImpl();
  }

  /**
   * Method returns the service channel that is used for the current environment. In this case a service channel for
   * distributed environment will be returned.
   * 
   * @return ServiceChannel Service channel implementation that is used for the current environment. The method never
   * returns null.
   * @see com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager#getServiceChannel()
   */
  @Override
  public ServiceChannel getServiceChannel( ) {
    return serviceChannel;
  }

  /**
   * Method returns the service registry that contains all available remote services.
   * 
   * @return ServiceRegistry Service registry with all available remote services. The method never returns null.
   * @see LifecycleManager#getServiceRegistry()
   */
  @Override
  public ServiceRegistry getServiceRegistry( ) {
    return serviceRegistry;
  }

  /**
   * Method returns the service provider registry.
   * 
   * @return {@link ServiceProviderRegistry} Service provider registry that will be used in this environment. The method
   * must not return null.
   */
  @Override
  public ServiceProviderRegistry getServiceProviderRegistry( ) {
    return serviceProviderRegistry;
  }

  /**
   * Method returns the activity registry.
   * 
   * @return {@link ActivityRegistry} Activity registry that will be used in this environment. The method must not
   * return null.
   */
  @Override
  public ActivityRegistry getActivityRegistry( ) {
    return activityRegistry;
  }

  /**
   * Method is called by the implementation of the base class of this lifecycle manager implementation in order to start
   * the initialization of JEAF. Thereby the method loads all available service from the application server via the
   * lifecycle manager ejb.
   * 
   * @see LifecycleManager#performStartup()
   */
  @Override
  public void performStartup( ) {
    try {
      // Lookup lifecycle manager EJB.
      LifecycleManagerEJB lLifecycleManagerEJB = EJBHelper.lookupEJB(lifecycleManagerJNDI, LifecycleManagerEJB.class);

      // Get class name of all available service and check which of them is loadable on the client side.
      Collection<String> lAllAvailableServiceNames = lLifecycleManagerEJB.getAllAvailableServiceNames();
      Vector<String> lSupportedServices = new Vector<String>(lAllAvailableServiceNames.size());
      for (String lClassName : lAllAvailableServiceNames) {
        if (Tools.getReflectionTools().isClassLoadable(lClassName)) {
          lSupportedServices.add(lClassName);
        }
      }

      // Get all supported services and add them to the service registry.
      Collection<ServiceProxy> lServiceProxies = lLifecycleManagerEJB.getServiceProxies(lSupportedServices);
      Iterator<ServiceProxy> lIterator = lServiceProxies.iterator();
      while (lIterator.hasNext()) {
        // Get next service proxy and add it to service registry.
        ServiceProxy lServiceProxy = lIterator.next();
        serviceRegistry.addServiceProxy(lServiceProxy);
      }

      // Get all message objects from EJB Container in order to avoid problem caused by not available error messages on
      // client side and add them to the local repository.
      MessageRepository lRemoteMessageRepository = lLifecycleManagerEJB.getRemoteMessageRepository();
      XFun.getMessageRepository().addAllMessages(lRemoteMessageRepository.getAllMessages());
    }
    // Catch exceptions and wrap it in SystemException.
    catch (RemoteException e) {
      throw new JEAFSystemException(MessageConstants.REMOTE_EXCEPTION_FROM_EJB_CALL, e,
          LifecycleManagerEJB.class.getName());
    }
  }

  /**
   * Method is called in order to shutdown.
   * 
   * @see LifecycleManager#performShutdown()
   */
  @Override
  protected void performShutdown( ) {
    // Nothing to do.
  }

  /**
   * Method returns the component to which the passed service implementation belongs to.
   * 
   * @param pService Service implementation to which the providing component should be returned. The parameter must not
   * be null.
   * @return {@link ComponentImplementation} Component that provides the passed service implementation. The method never
   * returns null.
   */
  @Override
  public ComponentImplementation getComponent( ServiceImplementation pService ) {
    return null;
  }

  /**
   * Method returns all components that were loaded by this life cycle manager. Since this life cycle manager
   * implementation is not intended to be used within a component runtime container (like a EJB container) the method
   * always returns an empty collection.
   * 
   * @return Collection All loaded components. The method never returns null and the collection can not be edited.
   */
  @Override
  public Collection<Component> getComponents( ) {
    return Collections.emptyList();
  }

  /**
   * Method returns the class objects of all services that were loaded by this life cycle manager. Since this life cycle
   * manager implementation is not intended to be used within a component runtime container (like a EJB container) the
   * method always returns an empty collection.
   * 
   * @return {@link Collection} Class objects of all loaded services. The method never returns null and the collection
   * can not be edited.
   */
  @Override
  public Collection<Class<? extends Service>> getServices( ) {
    return Collections.emptyList();
  }

  /**
   * Method returns the class objects of all service providers that were loaded by this life cycle manager. Since this
   * life cycle manager implementation is not intended to be used within a component runtime container (like a EJB
   * container) the method always returns an empty collection.
   * 
   * @return {@link Collection} Class objects of all loaded service providers. The method never returns null and the
   * collection can not be edited.
   */
  @Override
  public Collection<Class<? extends ServiceProvider>> getServiceProviders( ) {
    return Collections.emptyList();
  }

  /**
   * Method checks the current state of the service implementation that provides the passed service interface.
   * 
   * @param pService Service whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service does not implement any checks.
   */
  @Override
  public HealthCheckResult checkService( Class<? extends Service> pService, CheckLevel pCheckLevel ) {
    return null;
  }

  /**
   * Method checks the current state of the service provider implementation that provides the passed service provider
   * interface.
   * 
   * @param pServiceProvider Service Provider whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service provider does not implement any checks.
   */
  @Override
  public HealthCheckResult checkServiceProvider( Class<? extends ServiceProvider> pServiceProvider,
      CheckLevel pCheckLevel ) {
    return null;
  }

  /**
   * Method checks if this lifecycle manager implementation supports scheduling. As this implementations does not
   * support scheduling the method always returns false.
   * 
   * @return boolean Method returns always returns false.
   */
  @Override
  public final boolean supportsScheduling( ) {
    return false;
  }
}
