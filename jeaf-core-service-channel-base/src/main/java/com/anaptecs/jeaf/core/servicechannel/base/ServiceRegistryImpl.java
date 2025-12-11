/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProxy;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceRegistry;
import com.anaptecs.jeaf.core.spi.ServiceImplementation;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;

/**
 * Service registry contains all service implementations and the corresponding proxy objects.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public final class ServiceRegistryImpl implements ServiceRegistry {
  /**
   * Map contains all service instances. Thereby the class object of the service interface is used as key and the
   * service implementation as value.
   */
  private final Map<Class<? extends Service>, ServiceImplementation> services;

  /**
   * Map contains the proxy objects for all service objects.
   */
  private final Map<Class<? extends Service>, ServiceProxy> proxies;

  /**
   * Initialize object.
   */
  public ServiceRegistryImpl( ) {
    // Initialize maps.
    services = new HashMap<Class<? extends Service>, ServiceImplementation>();
    proxies = new HashMap<Class<? extends Service>, ServiceProxy>();
  }

  /**
   * Method registers the service that is provided via the passed service factory.
   * 
   * @param pServiceInstance Service instance that should be registered. The parameter must not be null.
   * @param pServiceProxy Service proxy that belongs to the service implementation. The parameter must not be null.
   */
  @Override
  public void registerService( ServiceImplementation pServiceInstance, ServiceProxy pServiceProxy ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pServiceInstance, "pServiceInstance");
    Check.checkInvalidParameterNull(pServiceProxy, "ServiceProxy");

    // Create new service instance and proxy and register them
    this.addServiceInstance(pServiceInstance);
    this.addServiceProxy(pServiceProxy);

    // Trace message
    XFun.getTrace().write(MessageConstants.CREATED_SERVICE, pServiceInstance.getClass().getName(),
        pServiceProxy.getClass().getName());
  }

  /**
   * Method adds the passed service INSTANCE as implementation for the corresponding service interface.
   * 
   * @param pServiceImplementation Service implementation that should be added to the service registry. The parameter
   * must not be null and a service INSTANCE of the same service interface must not have been added yet.
   * 
   * @see ServiceImplementation#getServiceType()
   */
  private void addServiceInstance( ServiceImplementation pServiceImplementation ) {
    // Check parameter.
    Assert.assertNotNull(pServiceImplementation, "pServiceImplementation");

    // Get class object of service interface.
    Class<? extends Service> lServiceType = pServiceImplementation.getServiceType();
    Assert.assertNotNull(lServiceType, "lServiceType");

    // Register service INSTANCE.
    if (services.containsKey(lServiceType) == false) {
      services.put(lServiceType, pServiceImplementation);
    }
    // Service INSTANCE for defined service INSTANCE already exists.
    else {
      String[] lParams = new String[] { pServiceImplementation.getClass().getName(), lServiceType.getName() };
      throw new JEAFSystemException(MessageConstants.SERVICE_INSTANCE_ALREADY_SET, lParams);
    }
  }

  /**
   * Method adds the passed service proxy as implementation for the corresponding service interface.
   * 
   * @param pServiceProxy Service proxy that should be added to the service registry. The parameter must not be null and
   * a service proxy of the same service interface must not have been added yet.
   * 
   * @see ServiceProxy#getServiceType()
   */
  private void addServiceProxy( ServiceProxy pServiceProxy ) {
    // Check parameter.
    Assert.assertNotNull(pServiceProxy, "pServiceProxy");

    // Get class object of service interface.
    Class<? extends Service> lServiceType = pServiceProxy.getServiceType();

    // Register service proxy.
    if (proxies.containsKey(lServiceType) == false) {
      proxies.put(lServiceType, pServiceProxy);
    }
    // Service proxy for defined service INSTANCE already exists.
    else {
      String[] lParams = new String[] { pServiceProxy.getClass().getName(), lServiceType.getName() };
      throw new JEAFSystemException(MessageConstants.SERVICE_INSTANCE_ALREADY_SET, lParams);
    }
  }

  /**
   * Method return a service instance that implements the passed service interface.
   * 
   * @param pServiceType Type of service whose implementation object should be returned. The parameter must not be null.
   * @return Service Service implementation of the passed type. The method never returns null.
   */
  @Override
  public ServiceImplementation getServiceInstance( Class<? extends Service> pServiceType ) {
    // Check parameter.
    Assert.assertNotNull(pServiceType, "pServiceType");

    // Get service INSTANCE for passed service type.
    ServiceImplementation lServiceInstance = (ServiceImplementation) services.get(pServiceType);

    // Return service INSTANCE.
    if (lServiceInstance != null) {
      return lServiceInstance;
    }
    // Service is not available.
    else {
      throw new JEAFSystemException(MessageConstants.SERVICE_NOT_AVAILABLE, pServiceType.getName());
    }
  }

  /**
   * Method returns all available service instances.
   * 
   * @return Collection All available service instances. The method never returns null and all objects within the
   * collection are INSTANCE of class com.anaptecs.jeaf.fwk.core.ServiceImplementation. The collection is immutable.
   */
  @Override
  public Collection<ServiceImplementation> getAllServiceInstances( ) {
    return Collections.unmodifiableCollection(services.values());
  }

  /**
   * Method returns a proxy object for the service that implements the passed service interface.
   * 
   * @param pServiceType Type of service whose proxy object should be returned. The parameter must not be null.
   * @return Service Service implementation of the passed type. The method never returns null.
   */
  @Override
  public ServiceProxy getServiceProxy( Class<? extends Service> pServiceType ) {
    // Check parameter.
    Assert.assertNotNull(pServiceType, "pServiceType");

    // Get service proxy for passed service type.
    ServiceProxy lServiceProxy = proxies.get(pServiceType);

    // Return service proxy.
    if (lServiceProxy != null) {
      return lServiceProxy;
    }
    // Service is not available.
    else {
      throw new JEAFSystemException(MessageConstants.SERVICE_NOT_AVAILABLE, pServiceType.getName());
    }
  }

  /**
   * Method returns all service proxies that are stored in this service registry.
   * 
   * @return {@link Collection} Collection with all service proxies of this service registry. The method never returns
   * null.
   */
  @Override
  public Collection<ServiceProxy> getAllServiceProxies( ) {
    return Collections.unmodifiableCollection(proxies.values());
  }

  /**
   * Method returns the class objects of all services that are available through this service registry.
   * 
   * @return Collection Class objects of all service interfaces that are available this service registry. The method
   * never returns null and all objects within the collection are instances of java.lang.Class.
   */
  @Override
  public Collection<Class<? extends Service>> getAvailableServiceClasses( ) {
    return Collections.unmodifiableCollection(services.keySet());
  }

  /**
   * Method checks if the service registry contains of service of the passed class.
   * 
   * @param pServiceClass Class of service that should be checked. The parameter must not be null
   * @return boolean Method returns true if the service registry contains the passed service and false in all other
   * cases.
   */
  @Override
  public boolean isServiceAvailable( Class<? extends Service> pServiceClass ) {
    // Check parameter.
    Check.checkInvalidParameterNull(pServiceClass, "pServiceClass");

    // Check if registry contains the passed service.
    return proxies.containsKey(pServiceClass);
  }
}
