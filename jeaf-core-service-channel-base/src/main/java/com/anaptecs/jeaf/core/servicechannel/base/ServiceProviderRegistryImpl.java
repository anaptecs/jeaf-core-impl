/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */

package com.anaptecs.jeaf.core.servicechannel.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.ServiceProvider;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProviderRegistry;
import com.anaptecs.jeaf.core.spi.ServiceProviderImplementation;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.ErrorCode;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;

/**
 * A service provider registry contains all available service providers. During startup of JEAF the registry will try to
 * load all configured service providers.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public final class ServiceProviderRegistryImpl implements ServiceProviderRegistry {
  /**
   * Collection contains all available service providers. The reference is never null. The map contains the class object
   * of the service provider interface as key and the service provider implementation object as value.
   */
  private final Map<Class<? extends ServiceProvider>, ServiceProviderImplementation> serviceProviders;

  /**
   * Initialize object.
   */
  public ServiceProviderRegistryImpl( ) {
    // Create new map to store all service providers.
    serviceProviders = new HashMap<>();
  }

  /**
   * Method registers the passed service provider as implementation for the passed service provider interface. Existing
   * service provider must not be overwritten with a new implementation.
   * 
   * @param pServiceProviderInterface Interface under which the passed service provider implementation should be
   * registered. The parameter must not be null.
   * @param pServiceProviderImpl Service provider implementation that should be registered. The parameter must not be
   * null.
   */
  @Override
  public void registerServiceProvider( Class<? extends ServiceProvider> pServiceProviderInterface,
      ServiceProviderImplementation pServiceProviderImpl ) {

    // Check parameters
    Check.checkInvalidParameterNull(pServiceProviderInterface, "pServiceProviderInterface");
    Check.checkInvalidParameterNull(pServiceProviderImpl, "pServiceProviderImpl");

    // Ensure that existing implementations will not be overwritten.
    if (serviceProviders.containsKey(pServiceProviderInterface) == false) {
      serviceProviders.put(pServiceProviderInterface, pServiceProviderImpl);
    }
    // Trace warning about potential class path issue.
    else {
      XFun.getTrace().write(MessageConstants.OTHER_SERVICE_PROVIDER_IMPL_ALREADY_REGISTERED, pServiceProviderImpl
          .getClass().getName(), pServiceProviderInterface.getClass().getName(), serviceProviders.get(
              pServiceProviderInterface).getClass().getName());
    }
  }

  /**
   * Method returns the class objects of all service providers that are available within this registry.
   * 
   * @return {@link Collection} Class object of a service provider interfaces. The method never returns null and the
   * collection is unmodifiable.
   */
  @Override
  public Collection<Class<? extends ServiceProvider>> getAvailableServiceProviderClasses( ) {
    return serviceProviders.keySet();
  }

  /**
   * Method performs a lookup for the service provider of the passed type.
   * 
   * @param <T> Type
   * @param pServiceProviderInterface Class object of the service provider interface for which a service provider
   * implementation object should be returned. The parameter must not be null.
   * @return {@link ServiceProvider} Service provider implementation of the passed interface. The method never returns
   * null.
   * @throws JEAFSystemException if no service provider of the passed type is available.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T extends ServiceProvider> T getServiceProvider( Class<T> pServiceProviderInterface )
    throws JEAFSystemException {
    // Check parameter for null.
    Check.checkInvalidParameterNull(pServiceProviderInterface, "pServiceProviderInterface");

    ServiceProvider lServiceProvider = this.serviceProviders.get(pServiceProviderInterface);
    if (lServiceProvider != null) {
      return (T) lServiceProvider;
    }
    // Requested service provider is not available.
    else {
      ErrorCode lErrorCode = MessageConstants.SERVICE_PROVIDER_NOT_AVAILABLE;
      String[] lParams = new String[] { pServiceProviderInterface.getName() };
      throw new JEAFSystemException(lErrorCode, lParams);
    }
  }

  /**
   * Method performs a lookup for the service provider implementation of the passed type.
   * 
   * @param <T> Type
   * @param pServiceProviderInterface Class object of the service provider interface for which a service provider
   * implementation object should be returned. The parameter must not be null.
   * @return {@link ServiceProviderImplementation} Service provider implementation of the passed interface. The method
   * never returns null.
   * @throws JEAFSystemException if no service provider of the passed type is available.
   */
  @Override
  public <T extends ServiceProvider> ServiceProviderImplementation getServiceProviderImpl(
      Class<T> pServiceProviderInterface ) {
    // Check parameter for null.
    Check.checkInvalidParameterNull(pServiceProviderInterface, "pServiceProviderInterface");

    ServiceProviderImplementation lServiceProvider = this.serviceProviders.get(pServiceProviderInterface);
    if (lServiceProvider != null) {
      return lServiceProvider;
    }
    // Requested service provider is not available.
    else {
      ErrorCode lErrorCode = MessageConstants.SERVICE_PROVIDER_NOT_AVAILABLE;
      String[] lParams = new String[] { pServiceProviderInterface.getName() };
      throw new JEAFSystemException(lErrorCode, lParams);
    }

  }

  /**
   * Method returns all available service providers.
   * 
   * @return {@link Collection} All available service providers. The method never returns null.
   */
  @Override
  public Collection<ServiceProvider> getAllServiceProviders( ) {
    Collection<ServiceProvider> lServiceProviders = new ArrayList<>(serviceProviders.size());
    for (Entry<Class<? extends ServiceProvider>, ServiceProviderImplementation> lEntry : serviceProviders.entrySet()) {
      lServiceProviders.add(lEntry.getValue());
    }
    return lServiceProviders;
  }
}
