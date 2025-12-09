/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.client;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.EJBException;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.jee.commons.EJBHelper;
import com.anaptecs.jeaf.core.servicechannel.api.Command;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.EJBSystemException;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.ServiceChannelEJB;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;

/**
 * This class implements a service channel for distributed environments. Therefore it delegates all service calls
 * commands to a remote channel. This remote channel is realized a stateless session bean.
 * 
 * @author Tillmann Schall (TLS)
 * @version $ $LastChangedRevision: 11730 $
 */
public class RemoteServiceChannel implements ServiceChannel {
  /**
   * Reference to instance of service channel within the EJB container. The reference is never null since the lookup for
   * the bean is performed when the object is created.
   */
  private ServiceChannelEJB remoteServiceChannelEJB;

  /**
   * Initialize object. Thereby a reference to the service channel EJB will be obtained.
   * 
   * @param pJNDIName JNDI lookup name for service channel EJB. The parameter must not be null.
   */
  RemoteServiceChannel( String pJNDIName ) {
    // Check parameter.
    Assert.assertNotNull(pJNDIName, "pJNDIName");

    // Perform lookup for service channel EJB.
    remoteServiceChannelEJB = EJBHelper.lookupEJB(pJNDIName, ServiceChannelEJB.class);
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments). This method call through the service channel will be executed with transaction behavior
   * NOT_SUPPORTED.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public final Serializable executeCommandTxNotSupported( Command pCommand )
    throws ApplicationException, SystemException {
    try {
      return remoteServiceChannelEJB.executeCommandTxNotSupported(pCommand);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw this.handleRemoteException(e, pCommand);
    }
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments). This method call through the service channel will be executed with transaction behavior SUPPORTS.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public final Serializable executeCommandTxSupports( Command pCommand ) throws ApplicationException, SystemException {
    try {
      return remoteServiceChannelEJB.executeCommandTxSupports(pCommand);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw this.handleRemoteException(e, pCommand);
    }
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments). This method call through the service channel will be executed with transaction behavior REQUIRED.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public final Serializable executeCommandTxRequired( Command pCommand ) throws ApplicationException, SystemException {
    try {
      return remoteServiceChannelEJB.executeCommandTxRequired(pCommand);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw this.handleRemoteException(e, pCommand);
    }
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments). This method call through the service channel will be executed with transaction behavior
   * REQUIRES_NEW.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public final Serializable executeCommandTxRequiresNew( Command pCommand )
    throws ApplicationException, SystemException {
    try {
      return remoteServiceChannelEJB.executeCommandTxRequiresNew(pCommand);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw this.handleRemoteException(e, pCommand);
    }
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments). This method call through the service channel will be executed with transaction behavior MANDATORY.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public final Serializable executeCommandTxMandatory( Command pCommand ) throws ApplicationException, SystemException {
    try {
      return remoteServiceChannelEJB.executeCommandTxMandatory(pCommand);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw this.handleRemoteException(e, pCommand);
    }
  }

  /**
   * Method executes the passed command. Which actions are performed in detail depends on the service channel
   * implementation. The command may be executed within the same VM (J2SE environments) or on some remote host (J2EE
   * environments). This method call through the service channel will be executed with transaction behavior NEVER.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @return Serializable Result of the command execution. Since JEAF supports distributed environments all results must
   * be serializable.
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically rolled back.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rolled back.
   */
  public final Serializable executeCommandTxNever( Command pCommand ) throws ApplicationException, SystemException {
    try {
      return remoteServiceChannelEJB.executeCommandTxNever(pCommand);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw this.handleRemoteException(e, pCommand);
    }
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
    try {
      // The only that that has to be done is to call the base class' implementation of a service call.
      return remoteServiceChannelEJB.checkService(pServiceClass, pCheckLevel);
    }
    // Handle RemoteException. They always result in a system exception since remote exceptions indicate technical
    // problems within the infrastructure or application.
    catch (RemoteException e) {
      throw new EJBSystemException(MessageConstants.REMOTE_EXCEPTION_FROM_SERVICE_CALL, e,
          pServiceClass.getClass().getName());
    }
  }

  /**
   * Method handles the passed remote exception by analyzing the cause and returning the appropriate runtime exception.
   * 
   * @param pRemoteException RemoteException that occurred during the service call. The parameter must not be null.
   * @param pCommand Command object describing the service call. The parameter must not be null.
   * @return {@link RuntimeException} RuntimeException object as reaction on the occurred RemoteException. The method
   * never returns null.
   */
  private RuntimeException handleRemoteException( RemoteException pRemoteException, Command pCommand ) {
    // Check parameter
    Assert.assertNotNull(pRemoteException, "pRemoteException");
    Assert.assertNotNull(pCommand, "pCommand");

    // Determine the origin exception.
    RuntimeException lReaction;
    Throwable lCause = pRemoteException.getCause();

    // A SystemExceptin within the EJB container caused the remote exception.
    if (lCause instanceof EJBException) {
      EJBException lEJBException = (EJBException) lCause;
      Throwable lRealCause = lEJBException.getCause();
      if (lRealCause instanceof RuntimeException) {
        lReaction = (RuntimeException) lRealCause;
      }
      else {
        lReaction = new EJBSystemException(MessageConstants.REMOTE_EXCEPTION_FROM_SERVICE_CALL, lCause,
            pCommand.getTargetServiceClass().getName());
      }
    }
    else if (lCause instanceof RuntimeException) {
      lReaction = (RuntimeException) lCause;
    }
    // Any other cause for a remote exception as a NPE e.g.
    else if (lCause != null) {
      // Throw new SystemException with cause as nested exception.
      lReaction = new EJBSystemException(MessageConstants.REMOTE_EXCEPTION_FROM_SERVICE_CALL, lCause,
          pCommand.getTargetServiceClass().getName());
    }
    // Pure remote exception caused by communication problems etc.
    else {
      // Throw new SystemException with remote exception as nested exception.
      lReaction = new EJBSystemException(MessageConstants.REMOTE_EXCEPTION_FROM_SERVICE_CALL, pRemoteException,
          pCommand.getTargetServiceClass().getName());
    }

    // Return SystemException
    return lReaction;
  }
}
