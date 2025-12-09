/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import java.io.Serializable;

import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.jee.commons.EJBHelper;
import com.anaptecs.jeaf.core.servicechannel.api.Command;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.ServiceChannelEJBLocal;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;

/**
 * This class implements a service channel for EJB environments. Therefore it delegates all service calls commands to a
 * local EJB channel.
 * 
 * @author Tillmann Schall (TLS)
 * @version 1.0
 */
public final class EJBLocalServiceChannel implements ServiceChannel {
  /**
   * Reference to instance of service channel within the EJB container. The reference is never null since the lookup for
   * the bean is performed when the object is created.
   */
  private ServiceChannelEJBLocal localServiceChannelEJB;

  /**
   * Initialize object. Thereby a reference to the service channel EJB will be obtained.
   */
  EJBLocalServiceChannel( ) {
    // Lookup ServiceChannelEJB
    String lJNDIName = ServiceChannelEJBLocal.LOCAL_EJB_JNDI_NAME;
    localServiceChannelEJB = EJBHelper.lookupEJB(lJNDIName, ServiceChannelEJBLocal.class);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments).
   * 
   * This method call through the service channel will be executed with transaction behavior NOT_SUPPORTED.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public Serializable executeCommandTxNotSupported( Command pCommand ) throws ApplicationException, SystemException {
    return localServiceChannelEJB.executeCommandTxNotSupported(pCommand);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments).
   * 
   * This method call through the service channel will be executed with transaction behavior SUPPORTS.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public Serializable executeCommandTxSupports( Command pCommand ) throws ApplicationException, SystemException {
    return localServiceChannelEJB.executeCommandTxSupports(pCommand);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments).
   * 
   * This method call through the service channel will be executed with transaction behavior REQUIRED.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rollbacked.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rollbacked.
   */
  public Serializable executeCommandTxRequired( Command pCommand ) throws ApplicationException, SystemException {
    return localServiceChannelEJB.executeCommandTxRequired(pCommand);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments).
   * 
   * This method call through the service channel will be executed with transaction behavior REQUIRES_NEW.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public Serializable executeCommandTxRequiresNew( Command pCommand ) throws ApplicationException, SystemException {
    return localServiceChannelEJB.executeCommandTxRequiresNew(pCommand);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments).
   * 
   * This method call through the service channel will be executed with transaction behavior MANDATORY.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public Serializable executeCommandTxMandatory( Command pCommand ) throws ApplicationException, SystemException {
    return localServiceChannelEJB.executeCommandTxMandatory(pCommand);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments).
   * 
   * This method call through the service channel will be executed with transaction behavior NEVER.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public Serializable executeCommandTxNever( Command pCommand ) throws ApplicationException, SystemException {
    return localServiceChannelEJB.executeCommandTxNever(pCommand);
  }

  /**
   * Method checks the state of the passed service. Method checks the current state of the service implementation that
   * provides the passed service interface.
   * 
   * This method call through the service channel will be executed with transaction behavior REQUIRES_NEW.
   * 
   * @param pService Service whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service does not implement any checks.
   */
  public final HealthCheckResult checkService( Class<? extends Service> pServiceClass, CheckLevel pCheckLevel ) {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return localServiceChannelEJB.checkService(pServiceClass, pCheckLevel);
  }
}
