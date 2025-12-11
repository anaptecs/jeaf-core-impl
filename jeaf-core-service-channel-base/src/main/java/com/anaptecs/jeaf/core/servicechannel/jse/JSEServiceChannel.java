/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jse;

import java.io.Serializable;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.core.api.TxContext;
import com.anaptecs.jeaf.core.servicechannel.api.Command;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.base.AbstractServiceChannel;
import com.anaptecs.jeaf.core.spi.TransactionBehavior;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;
import com.anaptecs.jeaf.xfun.api.info.ApplicationInfo;

/**
 * Core implementation of JEAF's service channel interface. This class is a minimum implementation of a service channel
 * and so does not support environment specific features such as transactions or remote calls. The implementation of
 * this class is intended to just support local / in JVM service calls.
 * 
 * Instances of this class are state less and support multi threading.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public abstract class JSEServiceChannel extends AbstractServiceChannel {
  /**
   * Initialize object. Currently no actions are performed.
   * 
   * @param pLifecycleManager Lifecycle manager to which this service channel belongs to. The parameter must not be
   * null.
   */
  protected JSEServiceChannel( LifecycleManager pLifecycleManager ) {
    // Nothing to do.
    super(pLifecycleManager);
  }

  /**
   * Method creates a new instance of a service invocation context object. Depending on the current context the method
   * will either return a new CoreRootContext or CoreServiceInvocationContext object.
   * 
   * @param pTargetServiceClass Class object of service interface that is called. The parameter must not be null.
   * @param pComponent Component to which the represented service call belongs to. The parameter must not be null.
   * @param pTransactionBehavior Transaction behavior which is used for the current service call. The parameter must not
   * be null.
   * @param pInvokingApplication Information about the invoking application. The parameter must not be null.
   * @return ServiceInvocationContext Created service invocation context object. The method never returns null.
   * 
   * @see AbstractServiceChannel#createServiceInvocationContext()
   */
  @Override
  protected final ServiceInvocationContext createServiceInvocationContext( Class<? extends Service> pTargetServiceClass,
      Component pComponent, ApplicationInfo pInvokingApplication, SessionContext pSessionContext ) {
    // Get current service invocation context
    ServiceInvocationContext lParentContext = lifecycleManager.getContext().getServiceInvocationContext();

    // This is the first service call within this thread thus a new CoreRootContext has to be created.
    ServiceInvocationContext lNewContext;
    if (lParentContext == null) {
      lNewContext = new RootContextImpl(pTargetServiceClass, pComponent, pInvokingApplication, pSessionContext);
    }
    // Create new service invocation context.
    else {
      lNewContext = new ServiceInvocationContextImpl(lParentContext, pTargetServiceClass, pComponent,
          pInvokingApplication, pSessionContext);
    }
    // Return created context.
    return lNewContext;
  }

  /**
   * Method will be called whenever a previously created transaction context is about to be released. This means
   * depending on the transactions state that is will either be committed or rolled back (see
   * <code>TxContext.getRollbackOnly()</code>). It depends on the concrete implementation of the service channel,
   * whether actions have to performed or not. The method will be called before the transaction context will be released
   * and become invalid.
   * 
   * This class only provides an empty implementation.
   * 
   * @param pTxContext Transaction context that will be released. The parameter is never used since this service channel
   * implementation does not support transactions.
   */
  @Override
  protected void releasingTxContext( TxContext pTxContext ) {
    // Since this service channel does not support transactions this method has nothing to do.
  }

  /**
   * Method executes the passed command. For this implementation of a service channel this means that a local (in VM)
   * method call will be performed.
   * 
   * This method call through the service channel should be executed with transaction behavior NOT_SUPPORTED. However
   * this service channel implementation currently does not support transactions.
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
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel#executeCommandTxNotSupported(Command)
   */
  @Override
  public final Serializable executeCommandTxNotSupported( Command pCommand )
    throws ApplicationException, SystemException {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.NOT_SUPPORTED);
  }

  /**
   * Method executes the passed command. For this implementation of a service channel this means that a local (in VM)
   * method call will be performed.
   * 
   * This method call through the service channel should be executed with transaction behavior SUPPORTS. However this
   * service channel implementation currently does not support transactions.
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
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel#executeCommandTxSupports(Command)
   */
  @Override
  public final Serializable executeCommandTxSupports( Command pCommand ) throws ApplicationException, SystemException {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.SUPPORTS);
  }

  /**
   * Method executes the passed command. For this implementation of a service channel this means that a local (in VM)
   * method call will be performed.
   * 
   * This method call through the service channel should be executed with transaction behavior REQUIRED. However this
   * service channel implementation currently does not support transactions.
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
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel#executeCommandTxRequired(Command)
   */
  @Override
  public final Serializable executeCommandTxRequired( Command pCommand ) throws ApplicationException, SystemException {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.REQUIRED);
  }

  /**
   * Method executes the passed command. For this implementation of a service channel this means that a local (in VM)
   * method call will be performed.
   * 
   * This method call through the service channel should be executed with transaction behavior REQUIRES_NEW. However
   * this service channel implementation currently does not support transactions.
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
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel#executeCommandTxRequiresNew(Command)
   */
  @Override
  public final Serializable executeCommandTxRequiresNew( Command pCommand )
    throws ApplicationException, SystemException {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.REQUIRES_NEW);
  }

  /**
   * Method executes the passed command. For this implementation of a service channel this means that a local (in VM)
   * method call will be performed.
   * 
   * This method call through the service channel should be executed with transaction behavior MANDATORY. However this
   * service channel implementation currently does not support transactions.
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
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel#executeCommandTxMandatory(Command)
   */
  @Override
  public final Serializable executeCommandTxMandatory( Command pCommand ) throws ApplicationException, SystemException {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.MANDATORY);
  }

  /**
   * Method executes the passed command. For this implementation of a service channel this means that a local (in VM)
   * method call will be performed.
   * 
   * This method call through the service channel should be executed with transaction behavior NEVER. However this
   * service channel implementation currently does not support transactions.
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
   * @see com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel#executeCommandTxNever(Command)
   */
  @Override
  public final Serializable executeCommandTxNever( Command pCommand ) throws ApplicationException, SystemException {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.invokeService(pCommand, TransactionBehavior.NEVER);
  }

  /**
   * Method checks the state of the passed service. Method checks the current state of the service implementation that
   * provides the passed service interface.
   * 
   * This method call through the service channel should be executed with transaction behavior REQUIRES_NEW. However
   * this service channel implementation currently does not support transactions.
   * 
   * @param pService Service whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service does not implement any checks.
   */
  @Override
  public final HealthCheckResult checkService( Class<? extends Service> pServiceClass, CheckLevel pCheckLevel ) {
    // The only that that has to be done is to call the base class' implementation of a service call.
    return this.performServiceCheck(pServiceClass, pCheckLevel);
  }
}
