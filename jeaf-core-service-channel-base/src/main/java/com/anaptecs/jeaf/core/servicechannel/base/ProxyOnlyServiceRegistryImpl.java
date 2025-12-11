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
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.ErrorCode;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;

/**
 * Class implements a simple service registry that contains service proxies only.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public final class ProxyOnlyServiceRegistryImpl implements ServiceRegistry {
  /**
   * Map contains all the proxy objects for all remote services. The reference is never null, since a new Map object
   * will be created by the class' constructor. Within the map the class object of the service will be used as key and
   * the proxy object as value.
   */
  private final Map<Class<? extends Service>, ServiceProxy> serviceProxies;

  /**
   * Initialize object.
   */
  public ProxyOnlyServiceRegistryImpl( ) {
    serviceProxies = new HashMap<Class<? extends Service>, ServiceProxy>();
  }

  /**
   * Method registers the service that is provided via the passed service factory.
   * 
   * @param pServiceInstance Service instance that should be registered. The parameter must not be null.
   * @param pServiceProxy Service proxy that belongs to the service implementation. The parameter must not be null.
   */
  @Override
  public void registerService( ServiceImplementation pServiceInstance, ServiceProxy pServiceProxy ) {
  }

  /**
   * Method returns a proxy object for the service that implements the passed service interface.
   * 
   * @param pServiceType Type of service whose proxy object should be returned. The parameter must not be null.
   * @return Service Service implementation of the passed type. The method never returns null.
   * 
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceRegistry#getServiceProxy(java.lang.Class)
   */
  @Override
  public final ServiceProxy getServiceProxy( Class<? extends Service> pServiceType ) {
    // Check parameter.
    Assert.assertNotNull(pServiceType, "pServiceType");

    // Get service proxy for passed service type.
    ServiceProxy lServiceProxy = (ServiceProxy) serviceProxies.get(pServiceType);

    // Return service proxy.
    if (lServiceProxy != null) {
      return lServiceProxy;
    }
    // Service is not available.
    else {
      ErrorCode lErrorCode = MessageConstants.SERVICE_NOT_AVAILABLE;
      String[] lParams = new String[] { pServiceType.getName() };
      throw new JEAFSystemException(lErrorCode, lParams);
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
    return Collections.unmodifiableCollection(serviceProxies.values());
  }

  /**
   * Method adds the passed service proxy as implementation for the corresponding service interface.
   * 
   * @param pServiceProxy Service proxy that should be added to the service registry. The parameter must not be null and
   * a service proxy of the same service interface must not have been added yet.
   * 
   * @see ServiceProxy#getServiceType()
   */
  public final void addServiceProxy( ServiceProxy pServiceProxy ) {
    // Check parameter.
    Assert.assertNotNull(pServiceProxy, "pServiceProxy");

    // Get class object of service interface.
    Class<? extends Service> lServiceType = pServiceProxy.getServiceType();

    // Register service proxy.
    if (serviceProxies.containsKey(lServiceType) == false) {
      serviceProxies.put(lServiceType, pServiceProxy);
    }
    // Service proxy for defined service instance already exists.
    else {
      ErrorCode lErrorCode = MessageConstants.SERVICE_INSTANCE_ALREADY_SET;
      String[] lParams = new String[] { pServiceProxy.getClass().toString(), lServiceType.getClass().toString() };
      throw new JEAFSystemException(lErrorCode, lParams);
    }
  }

  /**
   * Method adds the passed service proxy as implementation for the corresponding service interface.
   * 
   * @param pServiceProxes Collection with service proxies that should be added to the service registry. The parameter
   * must not be null and a service proxy of the same service interface must not have been added yet.
   */
  public void addServiceProxies( Collection<ServiceProxy> pServiceProxies ) {
    // Check parameter.
    Assert.assertNotNull(pServiceProxies, "pServiceProxies");

    // Handle all passed service proxies.
    for (ServiceProxy lNextProxy : pServiceProxies) {
      this.addServiceProxy(lNextProxy);
    }
  }

  /**
   * Method returns the class objects of all services that are available through this service registry.
   * 
   * @return Collection Class objects of all service interfaces that are available this service registry. The method
   * never returns null and all objects within the collection are instances of java.lang.Class.
   */
  @Override
  public Collection<Class<? extends Service>> getAvailableServiceClasses( ) {
    return Collections.unmodifiableCollection(serviceProxies.keySet());
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
    return serviceProxies.containsKey(pServiceClass);
  }

  /**
   * Method clears the service registry. This means that the references to all services are removed.
   */
  public void clear( ) {
    serviceProxies.clear();
  }

  @Override
  public ServiceImplementation getServiceInstance( Class<? extends Service> pServiceType ) {
    Assert.internalError("Feature not supported by this implementation.");
    return null;
  }

  @Override
  public Collection<ServiceImplementation> getAllServiceInstances( ) {
    Assert.internalError("Feature not supported by this implementation.");
    return null;
  }
}
