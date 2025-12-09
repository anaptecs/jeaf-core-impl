/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */

package com.anaptecs.jeaf.core.servicechannel.base;

import static com.anaptecs.jeaf.core.servicechannel.base.TxStatus.NO_TX_RUNNING;
import static com.anaptecs.jeaf.core.servicechannel.base.TxStatus.TX_RUNNING;
import static com.anaptecs.jeaf.core.spi.TransactionBehavior.MANDATORY;
import static com.anaptecs.jeaf.core.spi.TransactionBehavior.NEVER;
import static com.anaptecs.jeaf.core.spi.TransactionBehavior.NOT_SUPPORTED;
import static com.anaptecs.jeaf.core.spi.TransactionBehavior.REQUIRED;
import static com.anaptecs.jeaf.core.spi.TransactionBehavior.REQUIRES_NEW;
import static com.anaptecs.jeaf.core.spi.TransactionBehavior.SUPPORTS;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.core.api.TxContext;
import com.anaptecs.jeaf.core.servicechannel.api.Command;
import com.anaptecs.jeaf.core.servicechannel.api.ContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceInvocationContextManager;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceRegistry;
import com.anaptecs.jeaf.core.servicechannel.api.TransactionContextManager;
import com.anaptecs.jeaf.core.spi.ComponentImplementation;
import com.anaptecs.jeaf.core.spi.ServiceCall;
import com.anaptecs.jeaf.core.spi.ServiceChannelInterceptor;
import com.anaptecs.jeaf.core.spi.ServiceImplementation;
import com.anaptecs.jeaf.core.spi.TransactionBehavior;
import com.anaptecs.jeaf.core.spi.TransactionListener;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.ErrorCode;
import com.anaptecs.jeaf.xfun.api.errorhandling.FailureMessage;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;
import com.anaptecs.jeaf.xfun.api.health.HealthStatus;
import com.anaptecs.jeaf.xfun.api.info.ApplicationInfo;
import com.anaptecs.jeaf.xfun.api.messages.MessageID;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This class is an abstract implementation of a JEAF service channel. Therefore it already provides all mechanisms for
 * calls of services. However this class does not provide any platform specific mechanisms. This has to be provide by
 * concrete implementations.
 * 
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public abstract class AbstractServiceChannel implements ServiceChannel {

  /**
   * 2 dimensional array is used as decision table that contains the information which transition has to be performed to
   * the current transaction context in the case that a service will be called. The values within the transition table
   * are equivalent to the definitions of the EJB standard concerning passing the transaction context between EJB calls
   * as describer in chapter 17.6.2 (page 385) within the EJB 2.1 standard.
   */
  private static final TxContextTransition[][] TX_CONTEXT_TRANSITIONS;

  /**
   * Reference to lifecycle manager.
   */
  protected final LifecycleManager lifecycleManager;

  /**
   * List contains all global service channel interceptors.
   */
  private final List<ServiceChannelInterceptor> globalInterceptors;

  /**
   * List contains all transaction listeners that are configured.
   */
  private final List<TransactionListener> transactionListeners;

  static {
    // NotSupported, Supports, Required, RequiresNew, Mandatory, Never
    TX_CONTEXT_TRANSITIONS = new TxContextTransition[TxStatus.values().length][TransactionBehavior.values().length];

    // Define reaction for transaction behavior "NotSupported".
    TX_CONTEXT_TRANSITIONS[NO_TX_RUNNING.ordinal()][NOT_SUPPORTED.ordinal()] = TxContextTransition.UNCHANGED;
    TX_CONTEXT_TRANSITIONS[TX_RUNNING.ordinal()][NOT_SUPPORTED.ordinal()] = TxContextTransition.NO_TX_REQUIRED;

    // Define reaction for transaction behavior "Required".
    TX_CONTEXT_TRANSITIONS[NO_TX_RUNNING.ordinal()][REQUIRED.ordinal()] = TxContextTransition.NEW_TX_REQUIRED;
    TX_CONTEXT_TRANSITIONS[TX_RUNNING.ordinal()][REQUIRED.ordinal()] = TxContextTransition.UNCHANGED;

    // Define reaction for transaction behavior "Supports".
    TX_CONTEXT_TRANSITIONS[NO_TX_RUNNING.ordinal()][SUPPORTS.ordinal()] = TxContextTransition.UNCHANGED;
    TX_CONTEXT_TRANSITIONS[TX_RUNNING.ordinal()][SUPPORTS.ordinal()] = TxContextTransition.UNCHANGED;

    // Define reaction for transaction behavior "RequiresNew".
    TX_CONTEXT_TRANSITIONS[NO_TX_RUNNING.ordinal()][REQUIRES_NEW.ordinal()] = TxContextTransition.NEW_TX_REQUIRED;
    TX_CONTEXT_TRANSITIONS[TX_RUNNING.ordinal()][REQUIRES_NEW.ordinal()] = TxContextTransition.NEW_TX_REQUIRED;

    // Define reaction for transaction behavior "Mandatory".
    TX_CONTEXT_TRANSITIONS[NO_TX_RUNNING.ordinal()][MANDATORY.ordinal()] = TxContextTransition.ERROR;
    TX_CONTEXT_TRANSITIONS[TX_RUNNING.ordinal()][MANDATORY.ordinal()] = TxContextTransition.UNCHANGED;

    // Define reaction for transaction behavior "Never".
    TX_CONTEXT_TRANSITIONS[NO_TX_RUNNING.ordinal()][NEVER.ordinal()] = TxContextTransition.UNCHANGED;
    TX_CONTEXT_TRANSITIONS[TX_RUNNING.ordinal()][NEVER.ordinal()] = TxContextTransition.ERROR;
  }

  /**
   * Initialize object. Thereby all global interceptors will be created.
   * 
   * @param pLifecycleManager Lifecycle manager to which this service channel belongs to. The parameter must not be
   * null.
   */
  public AbstractServiceChannel( LifecycleManager pLifecycleManager ) {
    // Check parameter
    Check.checkInvalidParameterNull(pLifecycleManager, "pLifecycleManager");

    // Setup link to lifecycle manager.
    lifecycleManager = pLifecycleManager;

    // Determine all global interceptors.
    CoreConfiguration lConfiguration = CoreConfiguration.getInstance();
    List<ServiceChannelInterceptor> lGlobalInterceptors = lConfiguration.getGlobalInterceptors();
    globalInterceptors = Collections.unmodifiableList(lGlobalInterceptors);

    // Determine all global transaction listeners.
    List<TransactionListener> lTransactionListeners = lConfiguration.getTransactionListeners();
    transactionListeners = Collections.unmodifiableList(lTransactionListeners);
  }

  /**
   * Method creates a new instance of a service invocation context object. The method has to return a new instance for
   * every call. There must not be any kind of pooling of invocation context objects.
   * 
   * @param pTargetServiceClass Class object of service interface that is called. The parameter must not be null.
   * @param pComponent Component to which the represented service call belongs to. The parameter must not be null.
   * @param pInvokingApplication Information about the invoking application. The parameter must not be null.
   * @return {@link ServiceInvocationContext} Created service invocation context object. The method must not return
   * null.
   */
  protected abstract ServiceInvocationContext createServiceInvocationContext(
      Class<? extends Service> pTargetServiceClass, Component pComponent, ApplicationInfo pInvokingApplication,
      SessionContext pSessionContext );

  /**
   * Method will be called in order to tell the concrete service channel implementation that a new transaction context
   * has to be created. This could depending on the platform also mean that a new transaction has to be started. The
   * returned transaction context represents the created transaction.
   * 
   * @return {@link TxContext} Created transaction context object. The method must not return null.
   */
  protected abstract TxContext createTxContext( );

  /**
   * Method will be called whenever a previously created transaction context is about to be released. This means
   * depending on the transactions state that is will either be committed or rolled back (see
   * <code>TxContext.getRollbackOnly()</code>). It depends on the concrete implementation of the service channel,
   * whether actions have to performed or not. The method will be called before the transaction context will be released
   * and become invalid.
   * 
   * @param pTxContext Transaction context that will be release. The parameter is never null.
   */
  protected abstract void releasingTxContext( TxContext pTxContext );

  /**
   * Method executes the passed command as a local service call. The command will be sent to the service instance that
   * is stored in the service registry of this JVM. The method also handles the appropriate transaction handling for the
   * service to execute.
   * 
   * @param pCommand Command object that should be executed. The command objects contains all parameters that are
   * required to perform the corresponding service call. The parameter must not be null.
   * @param pTransactionBehavior Transaction behavior that is used for the current service call. The parameter is passed
   * for information only. This means it is required in order to publish the current transactional behavior to service
   * implementations through the service invocation context. The parameter must not be null.
   * @return {@link Serializable} Result of the command execution. The method may return null.
   * 
   * @throws ApplicationException Services may throw an ApplicationException in order to indicate an application
   * specific problem.The current transaction will not be automatically roll backed.
   * @throws SystemException Service may throw a SystemException in order to indicate technical problems. In the case of
   * an system exception the current transaction will be marked for roll back.
   */
  protected final Serializable invokeService( Command pCommand, TransactionBehavior pTransactionBehavior )
    throws ApplicationException, SystemException {
    // Check parameters.
    Assert.assertNotNull(pCommand, "pCommand");
    Assert.assertNotNull(pTransactionBehavior, "pTransactionBehavior");

    // Get target service instance. As method "getServiceInstance()" is an internal method the service instance can not
    // be retrieved via the lifecycle manager.
    ServiceRegistry lRegistry = lifecycleManager.getServiceRegistry();
    Class<? extends Service> lTargetServiceClass = pCommand.getTargetServiceClass();
    ServiceImplementation lTargetService = lRegistry.getServiceInstance(lTargetServiceClass);

    // Get current service invocation context
    ContextManager lContextManager = lifecycleManager.getContextManager();
    ServiceInvocationContextManager lServiceInvocationContextManager =
        lContextManager.getServiceInvocationContextManager();
    ServiceInvocationContext lPreviousServiceInvocationContext =
        lServiceInvocationContextManager.getCurrentServiceInvocationContext();

    // In the case of stacked service invocations the method still returns the application info of the application that
    // was the origin of the service call.
    ApplicationInfo lInvokingApplication;
    if (lPreviousServiceInvocationContext != null) {
      lInvokingApplication = lPreviousServiceInvocationContext.getInvokingApplication();
    }
    else {
      lInvokingApplication = pCommand.getInvokingApplication();
    }

    // Propagate session context to service invocation context.
    SessionContext lSessionContext;
    if (lPreviousServiceInvocationContext != null) {
      lSessionContext = lPreviousServiceInvocationContext.getSessionContext();
    }
    else {
      lSessionContext = pCommand.getSessionContext();
    }
    // This is the first service call within this thread thus a new CoreRootContext has to be created.
    ComponentImplementation lComponent = lifecycleManager.getComponent(lTargetService);
    ServiceInvocationContext lCurrentServiceInvocationContext =
        this.createServiceInvocationContext(lTargetServiceClass, lComponent, lInvokingApplication, lSessionContext);

    Assert.assertNotNull(lCurrentServiceInvocationContext, "lCurrentContext");

    lServiceInvocationContextManager.setToCurrentServiceInvocationContext(lCurrentServiceInvocationContext);

    // Get current transaction context.
    TransactionContextManager lTransactionContextManager = lContextManager.getTransactionContextManager();
    final TxContext lPreviousTxContext = lTransactionContextManager.getCurrentTransactionContext();
    TxContext lCurrentTxContext = lPreviousTxContext;

    // Determine which change has to be done to the current transaction context.
    final TxContextTransition lTxContextTransition = this.determineTxContextTransition(pTransactionBehavior);
    switch (lTxContextTransition) {
      // A new transaction context is required, so create it. The transaction will be committed when the called service
      // completed without exception.
      case NEW_TX_REQUIRED:
        lCurrentTxContext = this.createTxContext();
        lTransactionContextManager.setToCurrentTxContext(lCurrentTxContext);
        this.notifyStartedTransaction();
        break;

      // The current transaction context has to be suspended. The transaction will be activated again when the service
      // call completed.
      case NO_TX_REQUIRED:
        lTransactionContextManager.unsetAsCurrentTxContext(lCurrentTxContext);
        lCurrentTxContext = null;
        break;

      // No changes have to be done to the current transaction context.
      case UNCHANGED:
        // Nothing to do.
        break;

      // Unexpected enumeration value.
      default:
        final ErrorCode lErrorCode = MessageConstants.UNKNOWN_TX_CONTEXT_TRANSITION;
        String[] lParams = new String[] { lTxContextTransition.toString() };
        throw new JEAFSystemException(lErrorCode, lParams);
    }

    // Create object describing this service call.
    Method lServiceMethod = pCommand.getServiceMethod();
    Object[] lParameters = pCommand.getParameters();
    final ServiceCall lServiceCall = new ServiceCall(lTargetService, lTargetServiceClass, lServiceMethod, lParameters);

    boolean lServiceCallSuccessful = false;
    try {
      // Notify all interceptors about service call.
      for (ServiceChannelInterceptor lInterceptor : this.getAllServiceChannelInterceptors(lTargetService)) {
        lInterceptor.preServiceCall(lServiceCall);
      }

      // Set start timestamp
      lServiceCall.startingServiceCall();

      // Execute service call and return result.
      final Serializable lResult = pCommand.execute(lTargetService);
      lServiceCallSuccessful = true;

      // Set completed timestamp
      lServiceCall.serviceCallCompleted();

      // Notify all interceptors about service call.
      for (ServiceChannelInterceptor lInterceptor : this.getAllServiceChannelInterceptors(lTargetService)) {
        lInterceptor.postServiceCall(lServiceCall, lResult);
      }

      // Check if current transaction is marked for roll back and throw an exception in this case. This will cause the
      // transaction be to rolled back.
      // lCurrentTxContext may be null since there does not always have to exist a transaction.
      if (lCurrentTxContext != null) {
        // According to EJB 3.1 spec the method getRollbackOnly may only be called from some tx behaviors
        // (EJB 3.1: 13.6.2.8 Handling of setRollbackOnly Method)
        if (pTransactionBehavior != TransactionBehavior.SUPPORTS && pTransactionBehavior != NOT_SUPPORTED
            && pTransactionBehavior != NEVER) {
          if (lCurrentTxContext.getRollbackOnly() == true) {
            final ErrorCode lErrorCode = MessageConstants.TX_MARKED_FOR_ROLLBACK;
            final String[] lParams = new String[] { pCommand.getCalledServiceMethod() };
            throw new JEAFSystemException(lErrorCode, lParams);
          }
        }
      }
      // In all other cases the transaction has to complete normal (see finally block below).
      return lResult;
    }
    // In case that an ApplicationException occurs nothing has to be done, exception the appropriate notification of all
    // interceptors.
    catch (ApplicationException e) {
      // Set completed timestamp
      lServiceCall.serviceCallCompleted();

      // Notify all interceptors about service call.
      for (ServiceChannelInterceptor lInterceptor : this.getAllServiceChannelInterceptors(lTargetService)) {
        lInterceptor.postServiceCall(lServiceCall, e);
      }

      throw e;
    }

    // Catch system exceptions in order to roll back the current transaction.
    catch (SystemException e) {
      // Set completed timestamp
      lServiceCall.serviceCallCompleted();

      // Mark transaction for roll back and throw exception again.
      this.markTxForRollback(pCommand, lCurrentTxContext, lTxContextTransition, e);

      // Notify all interceptors about service call.
      for (ServiceChannelInterceptor lInterceptor : this.getAllServiceChannelInterceptors(lTargetService)) {
        lInterceptor.postServiceCall(lServiceCall, e);
      }

      throw e;
    }

    // In case of any other runtime exception the same has to be done.
    catch (RuntimeException e) {
      // Set completed timestamp
      lServiceCall.serviceCallCompleted();

      // Mark transaction for roll back and throw exception again.
      this.markTxForRollback(pCommand, lCurrentTxContext, lTxContextTransition, e);

      // Notify all interceptors about service call.
      for (ServiceChannelInterceptor lInterceptor : this.getAllServiceChannelInterceptors(lTargetService)) {
        lInterceptor.postServiceCall(lServiceCall, e);
      }

      throw e;
    }

    // In case of an error catch it and mark the current transaction for roll back.
    catch (Error e) {
      // Set completed timestamp
      lServiceCall.serviceCallCompleted();

      // Mark transaction for roll back and throw exception again.
      this.markTxForRollback(pCommand, lCurrentTxContext, lTxContextTransition, e);

      // Notify all interceptors about service call.
      for (ServiceChannelInterceptor lInterceptor : this.getAllServiceChannelInterceptors(lTargetService)) {
        lInterceptor.postServiceCall(lServiceCall, e);
      }

      throw e;
    }

    // After the service call completed no matter with which result all listeners have to be notified, that's why we
    // need the first finally block.
    finally {
      // Notify all listeners about completed service call.
      try {
        lCurrentServiceInvocationContext.notifyServiceCallCompleted(lServiceCallSuccessful);
      }
      // Mark current transaction for rollback only due to exception.
      catch (RuntimeException | Error e) {
        // Mark transaction for roll back and throw exception again.
        this.markTxForRollback(pCommand, lCurrentTxContext, lTxContextTransition, e);
        throw e;
      }

      //
      // Perform cleanup of thread local context objects no matter what happened before.
      //
      finally {
        // Cleanup transaction context.
        switch (lTxContextTransition) {
          // Transaction was started for this service call, so it either has to be committed or rolled back.
          case NEW_TX_REQUIRED:
            // Commit current transaction
            try {
              // Notify all transaction listeners that the transaction will be released.
              this.notifyReleasingTransaction(lCurrentTxContext);

              // Notify concrete implementation that we are about to release the current transaction context.
              this.releasingTxContext(lCurrentTxContext);
            }
            // No matter what happens, even in the case that an exception occurs the transaction context has to be
            // restored correctly.
            finally {
              // Restore previous transaction.
              if (lPreviousTxContext != null) {
                lTransactionContextManager.setToCurrentTxContext(lPreviousTxContext);
              }
              else {
                lTransactionContextManager.unsetAsCurrentTxContext(lCurrentTxContext);
              }
              lCurrentTxContext.invalidate();

              // We also have to cleanup completely if an exceptions occurs within the section above. This is why the
              // code from below is copied here.

              // Cleanup service invocation context.
              // Restore previous context and invalidate current context.
              if (lPreviousServiceInvocationContext != null) {
                lServiceInvocationContextManager
                    .setToCurrentServiceInvocationContext(lPreviousServiceInvocationContext);
                lCurrentServiceInvocationContext.invalidate();
              }
              // There was no previous context that why the new one will only be removed.
              else {
                lServiceInvocationContextManager
                    .unsetAsCurrentServiceInvocationContext(lCurrentServiceInvocationContext);
              }
            }
            break;

          // Previous transaction was suspended for this service call and now has to be activated again.
          case NO_TX_REQUIRED:
            lTransactionContextManager.setToCurrentTxContext(lPreviousTxContext);
            break;

          // In all other cases there is nothing to do.
          default:
            break;
        }

        // Cleanup service invocation context.
        // Restore previous context and invalidate current context.
        if (lPreviousServiceInvocationContext != null) {
          lServiceInvocationContextManager.setToCurrentServiceInvocationContext(lPreviousServiceInvocationContext);
          lCurrentServiceInvocationContext.invalidate();
        }
        // There was no previous context that why the new one will only be removed.
        else {
          lServiceInvocationContextManager.unsetAsCurrentServiceInvocationContext(lCurrentServiceInvocationContext);
        }
      }
    }
  }

  /**
   * Method checks the state of the passed service. Method checks the current state of the service implementation that
   * provides the passed service interface. Therefore JEAF provides a transaction context. The only action that will be
   * performed within the transaction is the check of the service.
   * 
   * @param pServiceClass Service whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service does not implement any checks.
   */
  protected final HealthCheckResult performServiceCheck( Class<? extends Service> pServiceClass,
      CheckLevel pCheckLevel ) {
    // Check parameters
    Check.checkInvalidParameterNull(pServiceClass, "pServiceClass");
    Check.checkInvalidParameterNull(pCheckLevel, "pCheckLevel");

    String lServiceName = pServiceClass.getSimpleName();
    HealthCheckResult lCheckResult;

    try {
      CheckCommand lCommand = new CheckCommand(pServiceClass, pCheckLevel);
      lCheckResult = (HealthCheckResult) this.invokeService(lCommand, REQUIRES_NEW);
    }
    // In fact ApplicationException can never occur during checks (see class ServiceImplementation.check(...) )
    catch (ApplicationException e) {
      String[] lMessageParameters = new String[] { lServiceName, e.getMessage() };
      FailureMessage lFailure = new FailureMessage(MessageConstants.EXCEPTION_DURING_CHECK, lMessageParameters, e);
      lCheckResult = new HealthCheckResult(HealthStatus.ERROR, null, lFailure);
    }
    catch (SystemException e) {
      String[] lMessageParameters = new String[] { lServiceName, e.getMessage() };
      FailureMessage lFailure = new FailureMessage(MessageConstants.EXCEPTION_DURING_CHECK, lMessageParameters, e);
      lCheckResult = new HealthCheckResult(HealthStatus.ERROR, null, lFailure);
    }
    // Trace result of service check.
    Trace lTrace = XFun.getTrace();
    if (lCheckResult != null) {
      lTrace.write(MessageConstants.SERVICE_CHECK_RESULT, lServiceName, lCheckResult.getHealthStatus().name());

      // Trace errors
      List<FailureMessage> lErrors = lCheckResult.getErrors();
      if (lErrors.size() > 0) {
        lTrace.write(MessageConstants.SERVICE_CHECK_ERRORS, lServiceName);
        for (FailureMessage lFailure : lErrors) {
          lTrace.write(lFailure.getMessageID(), lFailure.getMessageParameters());
        }
      }
      // Trace warnings
      List<FailureMessage> lWarnings = lCheckResult.getErrors();
      if (lWarnings.size() > 0) {
        lTrace.write(MessageConstants.SERVICE_CHECK_WARNINGS, lServiceName);
        for (FailureMessage lFailure : lWarnings) {
          lTrace.write(lFailure.getMessageID(), lFailure.getMessageParameters());
        }
      }
    }
    else {
      lTrace.write(MessageConstants.SERVICE_CHECK_RESULT, lServiceName, HealthStatus.UNKNOWN.name());
    }

    // Return result of check
    return lCheckResult;
  }

  /**
   * Method marks the passed transaction context for roll back and traces the passed exception in the case that the
   * stack trace may get lost in the case that the roll back will also throw an exception.
   * 
   * @param pCommand Command that was executed. The parameter must not be null.
   * @param pCurrentTxContext Current transaction context that has to be rolled back. The parameter may be null.
   * @param pTxContextTransition Transition that was performed on the transaction context. The parameter must not be
   * null.
   * @param pThrowable {@link Throwable} object that caused the transaction to be rolled back. The parameter must not be
   * null.
   */
  private void markTxForRollback( Command pCommand, TxContext pCurrentTxContext,
      final TxContextTransition pTxContextTransition, Throwable pThrowable ) {
    // Check parameters.
    Assert.assertNotNull(pCommand, "pCommand");
    Assert.assertNotNull(pTxContextTransition, "pTxContextTransition");
    Assert.assertNotNull(pThrowable, "pThrowable");

    // Mark transaction for roll back.
    if (pCurrentTxContext != null) {
      pCurrentTxContext.setRollbackOnly();

      // In order to avoid that exceptions will get lost due to problems with the roll back in the finally block below
      // the caught exception will be traced in the case that a roll back will be tried below.
      if (TxContextTransition.NEW_TX_REQUIRED.equals(pTxContextTransition) == true) {
        final MessageID lMessageID = MessageConstants.EXCEPTION_CAUSED_TX_ROLLBACK;
        XFun.getTrace().error(lMessageID, pThrowable, new String[] { pCommand.getCalledServiceMethod() });
      }
    }
  }

  /**
   * Method returns the current transaction status.
   * 
   * @return {@link TxStatus} TxStatus object describing the current transaction status. The method never returns null.
   */
  private TxStatus getCurrentTransactionStatus( ) {
    TxStatus lCurrentTxStatus;
    // Get current transaction context.
    TransactionContextManager lTransactionContextManager =
        lifecycleManager.getContextManager().getTransactionContextManager();
    TxContext lCurrentTransactionContext = lTransactionContextManager.getCurrentTransactionContext();
    if (lCurrentTransactionContext != null) {
      lCurrentTxStatus = TxStatus.TX_RUNNING;
    }
    else {
      lCurrentTxStatus = TxStatus.NO_TX_RUNNING;
    }
    // Return current transaction status.
    return lCurrentTxStatus;
  }

  /**
   * Method determines the transaction context transition for the passed transaction behavior based on the current
   * transaction status. In case that the method would have to return {@link TxContextTransition#ERROR} the method will
   * throw an exception.
   * 
   * @param pTransactionBehavior Transaction behavior of the next service that will be called from within the current
   * transaction context.
   * @return {@link TxContextTransition} Object describing the transition which has to be performed for the current
   * transaction context. The method never returns null.
   * 
   * @throws JEAFSystemException In the case that the method would have to return {@link TxContextTransition#ERROR}.
   */
  private TxContextTransition determineTxContextTransition( TransactionBehavior pTransactionBehavior )
    throws JEAFSystemException {
    // Check parameters
    Assert.assertNotNull(pTransactionBehavior, "pTransactionBehavior");

    // Determine tx context transition for the current transaction context.
    final TxStatus lTransactionStatus = this.getCurrentTransactionStatus();
    final TxContextTransition lTxContextTransition;
    lTxContextTransition = TX_CONTEXT_TRANSITIONS[lTransactionStatus.ordinal()][pTransactionBehavior.ordinal()];

    // In case that it is an invalid transition throw the exception here, so that it has not to be done in several
    // places.
    if (TxContextTransition.ERROR.equals(lTxContextTransition) == true) {
      ErrorCode lErrorCode = MessageConstants.INVALID_TX_CONTEXT_TRANSITION;
      String[] lParams = new String[] { pTransactionBehavior.toString(), lTransactionStatus.toString() };
      throw new JEAFSystemException(lErrorCode, lParams);
    }
    else {
      // Return transaction context transition.
      return lTxContextTransition;
    }
  }

  /**
   * Method returns all service channel interceptors for the passed target service. This includes component specific
   * interceptors as well as global interceptors.
   * 
   * @param pTargetService Service that should be called and for which the interceptors should be returned. The
   * parameter must not be null.
   * @return {@link Set} Set containing all interceptors. The method never returns null.
   */
  private List<ServiceChannelInterceptor> getAllServiceChannelInterceptors( ServiceImplementation pTargetService ) {
    // Get component specific interceptors.
    ComponentImplementation lComponent = lifecycleManager.getComponent(pTargetService);
    Set<ServiceChannelInterceptor> lSpecificInterceptors = lComponent.getServiceChannelInterceptors();

    // Compute size of list.
    int lCapacity = globalInterceptors.size() + lSpecificInterceptors.size();
    List<ServiceChannelInterceptor> lInterceptors = new ArrayList<ServiceChannelInterceptor>(lCapacity);

    // Add all interceptors to new list.
    lInterceptors.addAll(globalInterceptors);
    lInterceptors.addAll(lSpecificInterceptors);

    return lInterceptors;
  }

  /**
   * Method notifies all transaction listeners about the newly started transaction.
   */
  private void notifyStartedTransaction( ) {
    for (TransactionListener lListener : transactionListeners) {
      lListener.startedTransaction();
    }
  }

  /**
   * Method notifies all transaction listeners that the transaction will either be committed or rolled back.
   * 
   * @param pTxContext Current transaction conntext
   */
  private void notifyReleasingTransaction( TxContext pTxContext ) {
    for (TransactionListener lListener : transactionListeners) {
      if (pTxContext.getRollbackOnly() == false) {
        lListener.committingTransaction();
      }
      else {
        lListener.rollbackingTransaction();
      }
    }
  }
}