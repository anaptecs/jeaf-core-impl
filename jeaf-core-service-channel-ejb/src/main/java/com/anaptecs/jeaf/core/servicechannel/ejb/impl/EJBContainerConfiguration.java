/**
 * Copyright 2004 - 2020 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import java.lang.annotation.Annotation;
import java.util.List;

import com.anaptecs.jeaf.core.annotations.EJBContainerConfig;
import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.config.AnnotationBasedConfiguration;

public class EJBContainerConfiguration extends AnnotationBasedConfiguration<EJBContainerConfig> {
  /**
   * Initialize object.
   */
  public EJBContainerConfiguration( ) {
    this(EJBContainerConfig.EJB_CONTAINER_CONFIG_RESOURCE_NAME, JEAF.CORE_BASE_PATH, false);
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
  public EJBContainerConfiguration( String pCustomConfigurationResourceName, String pCustomConfigurationBasePackagePath,
      boolean pExceptionOnError ) {

    super(pCustomConfigurationResourceName, pCustomConfigurationBasePackagePath, pExceptionOnError);
  }

  @Override
  protected Class<EJBContainerConfig> getAnnotationClass( ) {
    return EJBContainerConfig.class;
  }

  @Override
  protected String getDefaultConfigurationClass( ) {
    // As there is no meaningful default value we return null.
    return null;
  }

  @Override
  public EJBContainerConfig getEmptyConfiguration( ) {
    return new EJBContainerConfig() {

      @Override
      public Class<? extends Annotation> annotationType( ) {
        return EJBContainerConfig.class;
      }

      @Override
      public String entityManagerJNDIRootPath( ) {
        return null;
      }
    };
  }

  @Override
  public List<String> checkCustomConfiguration( EJBContainerConfig pCustomConfiguration ) {
    // No special checks are required as entityManagerJNDIRootPath may be null.
    return null;
  }

  /**
   * Method returns the entity manager JNDI root path under which all entity manager factories of an application will be
   * placed.
   * 
   * @return String Root path for all entity managers in JNDI tree. The method may be null in case that the parameter is
   * not defined.
   */
  public String getEntityManagerJNDIRootPath( ) {
    String lEntityManagerJNDIRootPath = theConfig.entityManagerJNDIRootPath();
    if (Tools.getStringTools().isRealString(lEntityManagerJNDIRootPath)) {
      lEntityManagerJNDIRootPath = XFun.getConfigurationProvider().replaceSystemProperties(lEntityManagerJNDIRootPath);
    }
    else {
      lEntityManagerJNDIRootPath = null;
    }
    return lEntityManagerJNDIRootPath;
  }
}
