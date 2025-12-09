/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.application.base;

import com.anaptecs.jeaf.xfun.api.principal.PrincipalProvider;
import com.anaptecs.jeaf.xfun.api.principal.PrincipalProviderFactory;

/**
 * Class implements a factory for a principal provider for JEAF Applications ({@link JEAFApplicationPrincipalProvider}).
 * 
 * @author JEAF Development Team
 */
public class JEAFApplicationPrincipalProviderFactory implements PrincipalProviderFactory {

  /**
   * Method returns a {@link JEAFApplicationPrincipalProvider}.
   * 
   * @return {@link PrincipalProvider} Principal provider that resolves the current user using JEAF Application. The
   * method never returns null.
   */
  @Override
  public PrincipalProvider getPrincipalProvider( ) {
    return new JEAFApplicationPrincipalProvider();
  }
}
