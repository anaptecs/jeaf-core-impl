/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.jee.commons;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;

/**
 * Class offers methods that simplify the handling of ejbs.
 * 
 * @version 1.0
 */
public final class EJBHelper {
  /**
   * In order to ensure that no instances of this class are created, the constructor is private.
   */
  private EJBHelper( ) {
    // Nothing to do.
  }

  /**
   * Method performs a lookup for the ejb home interface that is bound to the passed JNDI name. Since the method also
   * narrows the found object to the passed class a direct cast to the corresponding home interface can be performed.
   * 
   * @param pJNDIName JNDI name to which the home interface is bound. The parameter must not be null.
   * @param pRemoteClass Class object of the expected home interface. The parameter must not be null.
   * 
   * @return EJBHome EJB home interface that is bound to the passed JNDI name. The method never returns null.
   */
  public static <T> T lookupEJBHome( String pJNDIName, Class<T> pRemoteClass ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pJNDIName, "pJNDIName");
    Check.checkInvalidParameterNull(pRemoteClass, "pRemoteClass");

    try {
      // Perform lookup and execute "remote cast".
      InitialContext lInitialContext = new InitialContext();
      Object lRemoteObject = lInitialContext.lookup(pJNDIName);

      // Remote object found. Now we have to cast it to the requested type and return it.
      if (lRemoteObject != null) {
        @SuppressWarnings("unchecked")
        T lObject = (T) PortableRemoteObject.narrow(lRemoteObject, pRemoteClass);
        return lObject;
      }
      // Lookup failed. No object found.
      else {
        throw new JEAFSystemException(MessageConstants.UNABLE_TO_LOOKUP_EJB, pJNDIName);
      }

    }
    // Catch NamingException and wrap it in a JEAFSystemException.
    catch (NamingException e) {
      throw new JEAFSystemException(MessageConstants.UNABLE_TO_LOOKUP_EJB, e, pJNDIName);
    }
  }

  /**
   * Method performs a lookup for the ejb interface that is bound to the passed JNDI name.
   * 
   * @param pJNDIName JNDI name to which the home interface is bound. The parameter must not be null.
   * @param pRemoteClass Class object of the expected interface. The parameter must not be null.
   * 
   * @return EJBHome EJB interface that is bound to the passed JNDI name. The method never returns null.
   */
  public static <T> T lookupEJB( String pJNDIName, Class<T> pRemoteClass ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pJNDIName, "pJNDIName");
    Check.checkInvalidParameterNull(pRemoteClass, "pRemoteClass");

    try {
      // Perform JNDI lookup for EJB.
      InitialContext lInitialContext = new InitialContext();
      @SuppressWarnings("unchecked")
      T lEJB = (T) lInitialContext.lookup(pJNDIName);

      // Remote object found. Now we have to cast it to the requested type and return it.
      if (lEJB != null) {
        return lEJB;
      }
      // Lookup failed. No object found.
      else {
        throw new JEAFSystemException(MessageConstants.UNABLE_TO_LOOKUP_EJB, pJNDIName);
      }
    }
    // Catch NamingException and wrap it in a JEAFSystemException.
    catch (NamingException e) {
      throw new JEAFSystemException(MessageConstants.UNABLE_TO_LOOKUP_EJB, e, pJNDIName);
    }
  }
}
