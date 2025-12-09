/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */

package com.anaptecs.jeaf.core.servicechannel.ejb.impl;

import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.security.auth.Subject;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.jaas.UserPrincipal;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.info.ApplicationInfo;
import com.anaptecs.jeaf.xfun.api.trace.ContextStackElement;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This class is an implementation of a service invocation context for JEE environments. Thus this class delegates some
 * of its calls the EJB session context.
 * 
 * @author Tillmann Schall (TLS)
 * @version 1.0
 */
class EJBServiceInvocationContext extends ServiceInvocationContext {
  /**
   * Default Serial Version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Reference to EJB session context. The reference is never null since the session context has to be passed to the
   * constructor.
   */
  private final transient SessionContext ejbSessionContext;

  /**
   * Initialize object. Therefore the EJB session context has to be passed.
   * 
   * @param pEJBSessionContext EJB session context for the current call. The parameter must not be null.
   * @param pTargetServiceClass Class object of service interface that is called. The parameter must not be null.
   * @param pComponent Component to which the represented service call belongs to. The parameter must not be null.
   * @param pInvokingApplication Information about the invoking application. The parameter must not be null.
   */
  EJBServiceInvocationContext( SessionContext pEJBSessionContext, Class<? extends Service> pTargetServiceClass,
      Component pComponent, ApplicationInfo pInvokingApplication,
      com.anaptecs.jeaf.core.api.SessionContext pJEAFSessionContext ) {

    // Call constructor of super class. There all parameters will be checked.
    super(pTargetServiceClass, pComponent, pInvokingApplication, pJEAFSessionContext);

    // Check parameter for null.
    Check.checkInvalidParameterNull(pEJBSessionContext, "pEJBSessionContext");

    // Assign session context.
    this.ejbSessionContext = pEJBSessionContext;

    // Manage trace stack of service invocation context objects.
    ServiceInvocationContext lCurrentContext = JEAF.getContext().getServiceInvocationContext();
    ContextStackElement lContext = new ContextStackElement(pTargetServiceClass.getName(), pComponent.getComponentID());
    Trace lCurrentTrace = XFun.getTrace();

    if (lCurrentContext != null) {
      lCurrentTrace.pushContextStackElement(lContext);
    }
    else {
      lCurrentTrace.newContextStack(lContext);
    }
  }

  /**
   * Method returns the principal of the caller in which the current service call is executed. The concrete principal
   * implementation is highly dependent on the used runtime environment.
   * 
   * @return Principal Principal object describing the caller in whose context the current service call is executed. The
   * method never returns null.
   */
  public Principal getCurrentPrincipal( ) {
    // We have to support 2 cases:
    // 1. A regular request through the service channel
    // 2. A request that arises from JEAF's scheduling. In this cases we do not have a caller. However in this case we
    // can resolve the current subject.
    Subject lSubject = Subject.getSubject(AccessController.getContext());

    // We have just another regular call through the service channel.
    Principal lPrincipal;
    if (lSubject == null) {
      lPrincipal = ejbSessionContext.getCallerPrincipal();
    }
    // JEAF's scheduling mechanism provided a subject
    else {
      // Get principal. We are expecting exactly one principal
      Set<UserPrincipal> lPrincipals = lSubject.getPrincipals(UserPrincipal.class);
      Assert.assertTrue(lPrincipals.size() == 1, "Subject " + lSubject.toString() + " has more than one principal.");
      lPrincipal = lPrincipals.iterator().next();
    }

    // Return principal of current user.
    return lPrincipal;
  }
}
