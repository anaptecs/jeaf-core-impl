/*
 * anaptecs GmbH, Ricarda-Huch-Str. 71, 72760 Reutlingen, Germany
 *
 * Copyright 2004 - 2019. All rights reserved.
 */
package com.anaptecs.jeaf.junit.core;

import com.anaptecs.jeaf.core.api.Service;

public interface MyCustomDefaultServiceBase extends Service {
  /**
   * @param pMessage
   * @return {@link String}
   */
  String doSomething( String pMessage );

  /**
   * @return {@link String}
   */
  String doSomeMore( );
}