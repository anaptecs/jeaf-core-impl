/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jaas;

import com.anaptecs.jeaf.xfun.api.locale.LocaleProvider;
import com.anaptecs.jeaf.xfun.api.locale.LocaleProviderFactory;

/**
 * Class implements factory for a user based locale provider ({@link UserPrincipalBasedLocaleProvider}).
 * 
 * @author JEAF Development Team
 */
public class UserPrincipalBasedLocaleProviderFactoryImpl implements LocaleProviderFactory {
  /**
   * Reference to used locale provider.
   */
  private final LocaleProvider localeProvider = new UserPrincipalBasedLocaleProvider();

  /**
   * Method returns a locale provider that resolve the locale from the current user.
   * 
   * @return {@link LocaleProvider} Implementation of locale provider that should be used. The method never returns
   * null.
   */
  @Override
  public LocaleProvider getLocaleProvider( ) {
    return localeProvider;
  }
}
