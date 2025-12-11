/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.servicechannel.JEAFCore;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProxy;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceRegistry;
import com.anaptecs.jeaf.core.servicechannel.base.SchedulingConfiguration;
import com.anaptecs.jeaf.core.servicechannel.base.SecurityConfiguration;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJB;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.messages.MessageID;
import com.anaptecs.jeaf.xfun.api.messages.MessageRepository;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 */
public class LifecycleManagerEJBBean implements SessionBean, TimedObject, LifecycleManagerEJB {
  private static final long MILLISECONDS = 1000L;

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
  public LifecycleManagerEJBBean( ) {
    // Nothing to do.
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
   */
  public void ejbCreate( ) {
    // Lockup settings for service access restrictions from JEAF properties.
    SecurityConfiguration lConfiguration = SecurityConfiguration.getInstance();
    restrictAccessToExportedServicesOnly = lConfiguration.restrictAccessToExportedServices();

    // Get all exported services.
    exportedServices.addAll(lConfiguration.getExportedServices());

    // Write trace message.
    if (restrictAccessToExportedServicesOnly == true) {
      Trace lTrace = XFun.getTrace();
      lTrace.info(MessageConstants.RESTRICTED_MODE);
    }
  }

  /**
   * Method will be called by the application when a time interval completed.
   */
  public void ejbTimeout( Timer pTimer ) {
    LifecycleManager lLifecycleManager = JEAFCore.getInstance().getLifecycleManager();
    if (lLifecycleManager instanceof EJBLifecycleManager) {
      ((EJBLifecycleManager) lLifecycleManager).fireTriggers();
    }
    else {
      Assert.internalError("Fatal configuration issue: Using " + this.getClass().getName() + " but not "
          + EJBLifecycleManager.class.getName() + " as lifecycle manager. Used lifecylcle manager implementation: "
          + lLifecycleManager.getClass().getName());
    }
  }

  /**
   * Method performs the initialization of JEAF. This method is intended to be called by a startup listener when the
   * application server is started.
   */
  public void initialize( ) {
    // Get lifecycle manager and initialize it if required.
    EJBLifecycleManager lLifecycleManager = (EJBLifecycleManager) JEAFCore.getInstance().getLifecycleManager();
    if (lLifecycleManager.isInitalized() == false) {
      // Initialize lifecycle manager
      lLifecycleManager.initialize();

      // Start timer service as scheduling mechanism.
      if (SchedulingConfiguration.getInstance().isJEAFSchedulingEnabled() == true) {
        // Determine trigger interval.
        int lTriggerInterval = lLifecycleManager.getTriggerInterval();
        MessageID lMessageID = MessageConstants.STARTING_SCHEDULING;
        XFun.getTrace().write(lMessageID, String.valueOf(lTriggerInterval));

        // Use JEE default scheduling mechanism.
        // Create a not persistent timer.
        final TimerService lTimerService = sessionContext.getTimerService();
        TimerConfig lTimerConfig = new TimerConfig(null, false);
        lTimerService.createIntervalTimer(lTriggerInterval * MILLISECONDS, lTriggerInterval * MILLISECONDS,
            lTimerConfig);
      }
    }
  }

  /**
   * Method returns the class names of all available services. The method gives clients the ability to check which of
   * the available services are usable on client side. There might be a difference between the available services on the
   * server and the support services on client side as not all classes may be available on the client.
   * 
   * @return {@link List} List with the class names of all services that are available in the server. The method never
   * returns null.
   */
  public List<String> getAllAvailableServiceNames( ) {
    // Get lifecycle manager.
    EJBLifecycleManager lLifecycleManager = (EJBLifecycleManager) JEAFCore.getInstance().getLifecycleManager();
    ServiceRegistry lServiceRegistry = lLifecycleManager.getServiceRegistry();

    Collection<Class<? extends Service>> lAvailableServiceClasses = lServiceRegistry.getAvailableServiceClasses();
    List<String> lServiceClassNames = new ArrayList<>(lAvailableServiceClasses.size());
    for (Class<? extends Service> lNextServiceClass : lAvailableServiceClasses) {
      // Get name of next service class.
      String lClassName = lNextServiceClass.getName();

      // Check if the service is exported and add it to available services if so.
      if (restrictAccessToExportedServicesOnly == true) {
        if (exportedServices.contains(lNextServiceClass) == true) {
          lServiceClassNames.add(lClassName);
        }
      }
      // Access is not restricted.
      else {
        lServiceClassNames.add(lClassName);
      }
    }

    // Return list with class names of all services.
    return lServiceClassNames;
  }

  /**
   * Method returns all service proxies for the services with the passed names.
   * 
   * @param pClassnames Class names of the services that should be returned. The parameter must not be null.
   * @return {@link List} Method returns all services proxies for the services with the passed names. The method never
   * returns null and all object within the vector are instance of class ServiceProxy. The method returns a vector
   * instead of a collection in order to fulfill the serialization demands of the EJB standard.
   */
  public List<ServiceProxy> getServiceProxies( List<String> pClassNames ) {
    // Check parameter
    Check.checkInvalidParameterNull(pClassNames, "pClassNames");

    List<ServiceProxy> lServiceProxies = new ArrayList<>(pClassNames.size());
    for (String lNextServiceClassName : pClassNames) {
      // Load service class and lookup its proxy.
      Class<? extends Service> lServiceClass =
          Tools.getReflectionTools().loadClass(lNextServiceClassName, Service.class);
      ServiceProxy lServiceProxy = (ServiceProxy) JEAF.getService(lServiceClass);

      // Check if access to services is restricted and the requested service is exported.
      if (restrictAccessToExportedServicesOnly == true) {
        if (exportedServices.contains(lServiceClass) == true) {
          lServiceProxies.add(lServiceProxy);
        }
      }
      // Access to services is not restricted.
      else {
        lServiceProxies.add(lServiceProxy);
      }
    }

    // Returned all requested service proxies
    return lServiceProxies;
  }

  /**
   * Method returns the message repository of the ejb container to the caller. This method is required in order to have
   * all server side messages also available on client side.
   * 
   * @return {@link MessageRepository} Message repository with all the message objects that are loaded inside the EJB
   * container. The method never returns null.
   */
  public MessageRepository getRemoteMessageRepository( ) {
    return XFun.getMessageRepository();
  }
}
