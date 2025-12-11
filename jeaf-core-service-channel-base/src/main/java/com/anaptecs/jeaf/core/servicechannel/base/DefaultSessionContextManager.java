/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.core.servicechannel.api.SessionContextManager;

/**
 * Class provides a default implementation of a session context manager. However this session context manager
 * implementation is only sufficient for single user environments.
 * 
 * @author JEAF Development Team
 */
public class DefaultSessionContextManager implements SessionContextManager {
  /**
   * Single user session context.
   */
  private final SessionContext sessionContext = new SessionContext();

  @Override
  public SessionContext getSessionContext( ) {
    return sessionContext;
  }

  @Override
  public boolean isSessionContextAvailable( ) {
    return true;
  }
}
