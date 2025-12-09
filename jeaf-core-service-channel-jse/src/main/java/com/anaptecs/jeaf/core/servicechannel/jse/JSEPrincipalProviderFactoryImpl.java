/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jse;

import com.anaptecs.jeaf.xfun.api.principal.PrincipalProvider;
import com.anaptecs.jeaf.xfun.api.principal.PrincipalProviderFactory;

public class JSEPrincipalProviderFactoryImpl implements PrincipalProviderFactory {
  private final PrincipalProvider principalProvider = new JSEPrincipalProviderImpl();

  @Override
  public PrincipalProvider getPrincipalProvider( ) {
    return principalProvider;
  }
}
