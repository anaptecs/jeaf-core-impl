/**
 * Copyright 2004 - 2020 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.client;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.anaptecs.jeaf.core.annotations.EJBClientServiceChannelConfig;
import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.config.AnnotationBasedConfiguration;

/**
 * Class is responsible to provider configuration for EJB Client environments.
 * 
 * @author JEAF Development Team
 */
public class EJBClientServiceChannelConfiguration extends AnnotationBasedConfiguration<EJBClientServiceChannelConfig> {

  /**
   * Initialize object.
   */
  public EJBClientServiceChannelConfiguration( ) {
    this(EJBClientServiceChannelConfig.EJB_CLIENT_SERVICE_CHANNEL_CONFIG_RESOURCE_NAME, JEAF.CORE_BASE_PATH, false);
  }

  /**
   * Initialize object. During initialization configuration will be loaded.
   * 
   * @param pCustomConfigurationResourceName Name of the file which contains the class name of the custom configuration
   * class. The parameter must not be null.
   * @param pBasePackagePath Path under which the file should be found in the classpath. The parameter may be null.
   * @param pExceptionOnError If parameter is set to true then an exception will be thrown in case of configuration
   * errors.
   */
  public EJBClientServiceChannelConfiguration( String pCustomConfigurationResourceName,
      String pCustomConfigurationBasePackagePath, boolean pExceptionOnError ) {

    super(pCustomConfigurationResourceName, pCustomConfigurationBasePackagePath, pExceptionOnError);
  }

  @Override
  protected Class<EJBClientServiceChannelConfig> getAnnotationClass( ) {
    return EJBClientServiceChannelConfig.class;
  }

  @Override
  protected String getDefaultConfigurationClass( ) {
    // As there is no meaningful default we return null.
    return null;
  }

  @Override
  public EJBClientServiceChannelConfig getEmptyConfiguration( ) {
    return new EJBClientServiceChannelConfig() {

      @Override
      public Class<? extends Annotation> annotationType( ) {
        return EJBClientServiceChannelConfig.class;
      }

      @Override
      public String serviceChannelJNDI( ) {
        return null;
      }

      @Override
      public String providerURL( ) {
        return null;
      }

      @Override
      public String lifecycleManagerJNDI( ) {
        return null;
      }

      @Override
      public String initialContextFactory( ) {
        return null;
      }
    };
  }

  @Override
  public List<String> checkCustomConfiguration( EJBClientServiceChannelConfig pCustomConfiguration ) {
    List<String> lConfigurationProblems = new ArrayList<>(0);

    // Check if initial context factory is in class path
    String lInitialContextFactory = pCustomConfiguration.initialContextFactory();
    if (Tools.getStringTools().isRealString(lInitialContextFactory)) {
      boolean lClassLoadable = Tools.getReflectionTools().isClassLoadable(lInitialContextFactory);
      if (lClassLoadable == false) {
        lConfigurationProblems.add(
            "Configured InitialContextFactory for JNDI lookups could not be found in classpath (configured class: '"
                + lInitialContextFactory + "').");
      }
    }

    // Check if lifecycle manager JNDI name is configured.
    String lLifecycleManagerJNDI = pCustomConfiguration.lifecycleManagerJNDI();
    if (Tools.getStringTools().isRealString(lLifecycleManagerJNDI) == false) {
      lConfigurationProblems
          .add("JNDI name of lifecycle manager EJB is not configured. Please provide JNDI name of the EJB.");
    }

    // Check if service channel JNDI name is configured.
    String lServiceChannelJNDI = pCustomConfiguration.serviceChannelJNDI();
    if (Tools.getStringTools().isRealString(lServiceChannelJNDI) == false) {
      lConfigurationProblems
          .add("JNDI name of service channel EJB is not configured. Please provide JNDI name of the EJB.");
    }

    return lConfigurationProblems;
  }

  /**
   * Method returns initial context factory that should be used for JNDI lookups.
   * 
   * @return {@link String} Class name of the initial context factory that should be used for JNDI lookups. The method
   * may return null.
   */
  public String getInitialContextFactory( ) {
    String lInitialContextFactory = theConfig.initialContextFactory();
    if (Tools.getStringTools().isRealString(lInitialContextFactory)) {
      lInitialContextFactory = XFun.getConfigurationProvider().replaceSystemProperties(lInitialContextFactory);
    }
    else {
      lInitialContextFactory = null;
    }
    return lInitialContextFactory;
  }

  /**
   * Method returns the URL of the JNDI provider.
   * 
   * @return {@link String} URL of the JNDI provider. The method may return null.
   */
  public String getProviderURL( ) {
    String lProviderURL = theConfig.providerURL();
    if (Tools.getStringTools().isRealString(lProviderURL)) {
      lProviderURL = XFun.getConfigurationProvider().replaceSystemProperties(lProviderURL);
    }
    else {
      lProviderURL = null;
    }
    return lProviderURL;
  }

  /**
   * Method returns the JNDI name that should be used for lookups for lifecycle manager EJB.
   * 
   * @return {@link String} JNDI name for lookups of lifecycle manager EJB. The method never returns null.
   */
  public String getLifecycleManagerJNDI( ) {
    String lLifecycleManagerJNDI = theConfig.lifecycleManagerJNDI();
    if (Tools.getStringTools().isRealString(lLifecycleManagerJNDI)) {
      lLifecycleManagerJNDI = XFun.getConfigurationProvider().replaceSystemProperties(lLifecycleManagerJNDI);
    }
    else {
      lLifecycleManagerJNDI = null;
    }
    return lLifecycleManagerJNDI;
  }

  /**
   * Method returns the JNDI name that should be used for lookups for service channel EJB.
   * 
   * @return {@link String} JNDI name for lookups of service channel EJB. The method never returns null.
   */
  public String getServiceChannelJNDI( ) {
    String lServiceChannelJNDI = theConfig.serviceChannelJNDI();
    if (Tools.getStringTools().isRealString(lServiceChannelJNDI)) {
      lServiceChannelJNDI = XFun.getConfigurationProvider().replaceSystemProperties(lServiceChannelJNDI);
    }
    else {
      lServiceChannelJNDI = null;
    }
    return lServiceChannelJNDI;
  }
}
