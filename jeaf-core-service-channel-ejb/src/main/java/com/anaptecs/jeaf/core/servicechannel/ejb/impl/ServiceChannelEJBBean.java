/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.TxContext;
import com.anaptecs.jeaf.core.servicechannel.JEAFCore;
import com.anaptecs.jeaf.core.servicechannel.api.Command;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.base.AbstractServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.base.SecurityConfiguration;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.EJBSystemException;
import com.anaptecs.jeaf.core.spi.TransactionBehavior;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.ErrorCode;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;
import com.anaptecs.jeaf.xfun.api.info.ApplicationInfo;

/**
 * This class implements a JEAF service channel as a state less session bean. This implementation enhances the core
 * service channel with advanced features like transaction demarcation or remote invocation.
 */
public class ServiceChannelEJBBean extends AbstractServiceChannel implements SessionBean, ServiceChannel {

  /**
   * Serial version UID as required by Java's serialization mechanism. This constant has to be manually maintained.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Reference to session context that can be used to communicate with the EJB container, mainly for transaction
   * management. The reference will be set right before the call of a business method.
   */
  private SessionContext sessionContext;

  /**
   * Attribute defines whether the access to service should be restricted so that only explicitly exported service
   * should be accessible.
   */
  private boolean restrictAccessToExportedServicesOnly;

  /**
   * Set contains all services that are exported and thus may be called via the beans remote interface.
   */
  private Set<Class<? extends Service>> exportedServices = new HashSet<>();

  /**
   * Initialize object. Currently no actions are performed.
   */
  public ServiceChannelEJBBean( ) {
    super(JEAFCore.getInstance().getLifecycleManager());
  }

  /**
   * Method returns a reference to the current session context.
   * 
   * @return SessionContext Reference to session context that can be used to communicate with the EJB container, mainly
   * for transaction management. The method never returns null.
   */
  public SessionContext getSessionContext( ) {
    return sessionContext;
  }

  /**
   * Method will be called by the EJB container right before the call of a business method in order to provide the
   * current session context.
   * 
   * @param pSessionContext Reference to session context that can be used to communicate with the EJB container, mainly
   * for transaction management. The parameter must not be null.
   * 
   * @see SessionBean#setSessionContext(javax.ejb.SessionContext)
   */
  public void setSessionContext( SessionContext pSessionContext ) {
    // Check parameter for null.
    Assert.assertNotNull(pSessionContext, "pSessionContext");

    sessionContext = pSessionContext;
  }

  /**
   * Method will be called by the EJB container when this bean instance will be removed. Currently no actions are
   * performed.
   * 
   * @see javax.ejb.SessionBean#ejbRemove()
   */
  public void ejbRemove( ) {
    // Nothing to do.
  }

  /**
   * Method will be called by the EJB container to notify the bean about its activation. Currently no actions are
   * performed.
   * 
   * @see javax.ejb.SessionBean#ejbActivate()
   */
  public void ejbActivate( ) {
    // Nothing to do.
  }

  /**
   * Method will be called by the EJB container to notify the bean about its passivation. The method unsets the session
   * context reference.
   * 
   * @see javax.ejb.SessionBean#ejbPassivate()
   */
  public void ejbPassivate( ) {
    sessionContext = null;
  }

  /**
   * Default create method of the EJB.
   * 
   * @ejb.create-method
   */
  public void ejbCreate( ) {
    // Lockup settings for service access restrictions from JEAF properties.
    SecurityConfiguration lConfiguration = SecurityConfiguration.getInstance();
    restrictAccessToExportedServicesOnly = lConfiguration.restrictAccessToExportedServices();

    // Get all exported services.
    exportedServices.addAll(lConfiguration.getExportedServices());
  }

  /**
   * Method creates a new instance of a service invocation context object. This method returns a EJB based
   * implementation of a service invocation context.
   * 
   * @param pTargetServiceClass Class object of service interface that is called. The parameter must not be null.
   * @param pComponent Component to which the represented service call belongs to. The parameter must not be null.
   * @param pTransactionBehavior Transaction behavior which is used for the current service call. The parameter must not
   * be null.
   * @param pInvokingApplication Information about the invoking application. The parameter must not be null.
   * @return ServiceInvocationContext Created service invocation context object. The method never returns null.
   * 
   * @see com.anaptecs.jeaf.core.servicechannel.base.AbstractServiceChannel#createServiceInvocationContext()
   */
  @Override
  protected ServiceInvocationContext createServiceInvocationContext( Class<? extends Service> pTargetServiceClass,
      Component pComponent, ApplicationInfo pInvokingApplication,
      com.anaptecs.jeaf.core.api.SessionContext pSessionContext ) {

    // Create new EJB root service invocation context.
    return new EJBServiceInvocationContext(sessionContext, pTargetServiceClass, pComponent, pInvokingApplication,
        pSessionContext);
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
   * 
   * @ejb.interface-method view-type = "both"
   * @ejb.transaction type="NotSupported"
   */
  @Override
  public Serializable executeCommandTxNotSupported( Command pCommand ) throws ApplicationException, SystemException {
    // Check if access to target service is restricted. If the service is not accessible an exception will be thrown.
    this.checkServiceAccess(pCommand);

    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.NOT_SUPPORTED);
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
   * specific problem.The current transaction will not be automatically rollbacked.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rollbacked.
   * 
   * @ejb.interface-method view-type = "both"
   * @ejb.transaction type="Supports"
   */
  @Override
  public Serializable executeCommandTxSupports( Command pCommand ) throws ApplicationException, SystemException {
    // Check if access to target service is restricted. If the service is not accessible an exception will be thrown.
    this.checkServiceAccess(pCommand);

    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.SUPPORTS);
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
   * 
   * @ejb.interface-method view-type = "both"
   * @ejb.transaction type="Required"
   */
  @Override
  public Serializable executeCommandTxRequired( Command pCommand ) throws ApplicationException, SystemException {
    // Check if access to target service is restricted. If the service is not accessible an exception will be thrown.
    this.checkServiceAccess(pCommand);

    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.REQUIRED);
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
   * specific problem.The current transaction will not be automatically rollbacked.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rollbacked.
   * 
   * @ejb.interface-method view-type = "both"
   * @ejb.transaction type="RequiresNew"
   */
  @Override
  public Serializable executeCommandTxRequiresNew( Command pCommand ) throws ApplicationException, SystemException {
    // Check if access to target service is restricted. If the service is not accessible an exception will be thrown.
    this.checkServiceAccess(pCommand);

    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.REQUIRES_NEW);
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
   * specific problem.The current transaction will not be automatically rollbacked.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rollbacked.
   * 
   * @ejb.interface-method view-type = "both"
   * @ejb.transaction type="Mandatory"
   */
  @Override
  public Serializable executeCommandTxMandatory( Command pCommand ) throws ApplicationException, SystemException {
    // Check if access to target service is restricted. If the service is not accessible an exception will be thrown.
    this.checkServiceAccess(pCommand);

    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.MANDATORY);
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
   * specific problem.The current transaction will not be automatically rollbacked.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be rollbacked.
   * 
   * @ejb.interface-method view-type = "both"
   * @ejb.transaction type="Never"
   */
  @Override
  public Serializable executeCommandTxNever( Command pCommand ) throws ApplicationException, SystemException {
    // Check if access to target service is restricted. If the service is not accessible an exception will be thrown.
    this.checkServiceAccess(pCommand);

    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.NEVER);
  }

  /**
   * Method checks the state of the passed service. Method checks the current state of the service implementation that
   * provides the passed service interface.
   * 
   * @param pService Service whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service does not implement any checks.
   */
  @Override
  public HealthCheckResult checkService( Class<? extends Service> pServiceClass, CheckLevel pCheckLevel ) {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.performServiceCheck(pServiceClass, pCheckLevel);
  }

  /**
   * Method will be called in order to tell the concrete service channel implementation that a new transaction context
   * has to be created. This could depending on the platform also mean that a new transaction has to be started. The
   * returned transaction context represents the created transaction.
   * 
   * @return {@link TxContext} Created transaction context object. The method must not return null.
   */
  @Override
  protected TxContext createTxContext( ) {
    return new JEETxContext(sessionContext);
  }

  /**
   * Method will be called whenever a previously created transaction context is about to be released. This means
   * depending on the transactions state that is will either be committed or rolled back (see
   * <code>TxContext.getRollbackOnly()</code>). Due to the fact that in this environment the transaction handling is
   * provided by the application server namely the ejb container nothing has to be done within this method.
   * 
   * @param pTxContext Transaction context that will be release. The parameter is never null.
   */
  @Override
  protected void releasingTxContext( TxContext pTxContext ) {
    // Due to the fact that in this environment the transaction handling is provided by the application server namely
    // the ejb container nothing has to be done within this method.
  }

  /**
   * Method checks if the passed command is allowed to call the target service. Currently JEAF supports restrictions of
   * service calls for that arrive via the remote interface. Therefore a list of exported service can be defined in the
   * JEAf properties.
   * 
   * @param pCommand Command that contains the service call. The parameter must not be null.
   */
  private void checkServiceAccess( Command pCommand ) {
    // Check parameter
    Assert.assertNotNull(pCommand, "pCommand");

    // Is access restriction turned on?
    if (restrictAccessToExportedServicesOnly == true) {
      // Access to service is only restricted if the EJB is called via its remote interface.
      // TODO Remove workaround as soon as feature is supported by JBoss
      // Class lInvokedBusinessInterface = this.getSessionContext().getInvokedBusinessInterface();
      // if (ServiceChannelEJB.class.equals(lInvokedBusinessInterface) == true) {

      // As call of this.getSessionContext().getInvokedBusinessInterface() is not supported under JBoss 5.1.0 and 6.0.0
      // we use a workaround based on the application id of this application and the calling one.
      String lThisApplicationID = XFun.getInfoProvider().getApplicationInfo().getApplicationID();
      String lCallingApplicationID = pCommand.getInvokingApplication().getApplicationID();
      if (lThisApplicationID.equals(lCallingApplicationID) == false) {
        // Check if called service is in whitelist.
        Class<? extends Service> lTargetServiceClass = pCommand.getTargetServiceClass();
        if (exportedServices.contains(lTargetServiceClass) == false) {
          ErrorCode lErrorCode = MessageConstants.SERVICE_NOT_EXPORTED;
          throw new EJBSystemException(lErrorCode, new String[] { lTargetServiceClass.getName() });
        }
      }
    }
  }
}
