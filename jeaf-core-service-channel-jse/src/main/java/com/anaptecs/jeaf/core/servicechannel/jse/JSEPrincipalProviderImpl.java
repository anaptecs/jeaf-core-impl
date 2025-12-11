/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jse;

import java.security.Principal;

import com.anaptecs.jeaf.core.api.jaas.UserPrincipal;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.principal.PrincipalProvider;

/**
 * This class implements a principal provider that uses the JEAF User Management component.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public class JSEPrincipalProviderImpl implements PrincipalProvider {
  /**
   * Current JEAFPrincipal object. The reference may be null.
   */
  private UserPrincipal principal;

  /**
   * Initialize object.
   */
  public JSEPrincipalProviderImpl( ) {
    // Nothing to do.
  }

  /**
   * Method returns the current principal object. This means for this implementation that a principal object will be
   * returned that represents the user under which this JVM is run.
   * 
   * @return {@link Principal} Current principal object. The method may return null.
   */
  @Override
  public Principal getCurrentPrincipal( ) {
    return principal;
  }

  /**
   * Method sets the passed JEAFUserPrincipal to the current.
   * 
   * @param pPrincipal
   */
  public void setCurrentPrincipal( UserPrincipal pPrincipal ) {
    // Check parameter
    Assert.assertNotNull(pPrincipal, "pPrincipal");

    principal = pPrincipal;
  }

  /**
   * Method sets the principal reference to null.
   */
  public void unsetCurrentPrincipal( ) {
    principal = null;
  }
}
