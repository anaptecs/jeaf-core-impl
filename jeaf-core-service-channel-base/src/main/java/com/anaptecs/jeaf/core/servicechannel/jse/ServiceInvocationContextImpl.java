/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */

package com.anaptecs.jeaf.core.servicechannel.jse;

import java.security.Principal;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.info.ApplicationInfo;
import com.anaptecs.jeaf.xfun.api.trace.ContextStackElement;

/**
 * @author JEAF Development Team
 * @version 1.0
 */
final class ServiceInvocationContextImpl extends ServiceInvocationContext {
  /**
   * Default Serial Version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Reference to the parent context object. The reference is never null since a not null parent object has to be passed
   * to the constructor of this class.
   */
  private final ServiceInvocationContext parent;

  /**
   * Initialize object. Therefore a reference top the parent context has to be passed.
   * 
   * @param pParent Reference to parent context. The parameter must not be null.
   * @param pTargetServiceClass Class object of service interface that is called. The parameter must not be null.
   * @param pComponent Component to which the represented service call belongs to. The parameter must not be null.
   * @param pInvokingApplication Information about the invoking application. The parameter must not be null.
   * @param pSessionContext Session context that belongs to the current call.
   */
  ServiceInvocationContextImpl( ServiceInvocationContext pParent, Class<? extends Service> pTargetServiceClass,
      Component pComponent, ApplicationInfo pInvokingApplication, SessionContext pSessionContext ) {

    // Call constructor of super class. There all parameters will be checked.
    super(pTargetServiceClass, pComponent, pInvokingApplication, pSessionContext);

    // Check parameter for null.
    Assert.assertNotNull(pParent, "pParent");

    parent = pParent;

    // Push new context to context stack of trace
    ContextStackElement lContext = new ContextStackElement(pTargetServiceClass.getName(), pComponent.getComponentID());
    XFun.getTrace().pushContextStackElement(lContext);
  }

  /**
   * Method returns the principal of the caller in which the current service call is executed. The method call will be
   * delegated to the parent invocation context.
   * 
   * @return Principal Principal object describing the caller in whose context the current service call is executed. The
   * method never returns null.
   * 
   * @see com.anaptecs.jeaf.core.api.ServiceInvocationContext#getCurrentPrincipal()
   */
  public Principal getCurrentPrincipal( ) {
    // Ensure that this context object is still valid.
    this.checkValidity();

    // Delegate method call to parent context.
    return parent.getCurrentPrincipal();
  }
}
