/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jaas;

import java.security.Principal;
import java.util.Locale;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.ServiceInvocationContext;
import com.anaptecs.jeaf.core.api.jaas.UserPrincipal;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.locale.LocaleProvider;

/**
 * This class implements a JEAF Locale Provider that supports user specific language settings based on the
 * JEAFUserPrincipal. Therefore the class uses the principal of the current user.
 * 
 * @author JEAF Development Team
 */
public class UserPrincipalBasedLocaleProvider implements LocaleProvider {
  /**
   * Initialize locale provider. No actions are performed.
   */
  public UserPrincipalBasedLocaleProvider( ) {
    // Nothing to do.
  }

  /**
   * Method returns the locale of the current user. Therefore the JEAFUserPrincipal of the current user is determined.
   * 
   * @return Locale Locale that for the current user. The method never returns null.
   */
  @Override
  public Locale getCurrentLocale( ) {
    final Locale lUserLocale;

    // Try to get JEAFUserPrincipal of current user.
    ServiceInvocationContext lCurrentContext;
    try {
      lCurrentContext = JEAF.getContext().getServiceInvocationContext();
    }
    catch (Throwable e) {
      XFun.getTrace().writeEmergencyTrace(e.getMessage(), e);
      lCurrentContext = null;
    }
    if (lCurrentContext != null) {
      Principal lCurrentPrincipal = lCurrentContext.getCurrentPrincipal();
      if (lCurrentPrincipal instanceof UserPrincipal) {
        UserPrincipal lJEAFUserPrincipal = (UserPrincipal) lCurrentPrincipal;
        lUserLocale = lJEAFUserPrincipal.getUserLanguage();
      }
      // Use systems default locale as fallback.
      else {
        lUserLocale = Locale.getDefault();
      }
    }
    // Use systems default locale as fallback.
    else {
      lUserLocale = Locale.getDefault();
    }
    return lUserLocale;
  }
}
