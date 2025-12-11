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
import com.anaptecs.jeaf.xfun.api.info.ApplicationInfo;
import com.anaptecs.jeaf.xfun.api.trace.ContextStackElement;

/**
 * Class provides a service invocation context implementation. This implementation is intended to be only the first
 * service invocation context in a hierarchy of service calls.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
final class RootContextImpl extends ServiceInvocationContext {
  /**
   * Serial Version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Reference to the principal object representing the current user. The reference is never null since it is set within
   * the constructor.
   */
  private final Principal principal;

  /**
   * Initialize object. Thereby a new principal for the current user will be created.
   */
  RootContextImpl( Class<? extends Service> pTargetServiceClass, Component pComponent,
      ApplicationInfo pInvokingApplication, SessionContext pSessionContext ) {

    // Call constructor of super class. There all parameters will be checked.
    super(pTargetServiceClass, pComponent, pInvokingApplication, pSessionContext);

    // Set depth on the stack of service invocation context objects.
    ContextStackElement lContext = new ContextStackElement(pTargetServiceClass.getName(), pComponent.getComponentID());
    XFun.getTrace().newContextStack(lContext);

    // Create new principal for the current user.
    principal = XFun.getPrincipalProvider().getCurrentPrincipal();
  }

  /**
   * Method returns the principal of the caller in which the current service call is executed. The concrete principal
   * implementation is highly dependent on the used runtime environment.
   * 
   * @return Principal Principal object describing the caller in whose context the current service call is executed. The
   * method never returns null.
   */
  public Principal getCurrentPrincipal( ) {
    return principal;
  }
}
