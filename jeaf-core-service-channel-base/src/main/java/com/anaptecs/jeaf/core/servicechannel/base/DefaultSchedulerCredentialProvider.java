/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.servicechannel.api.SchedulerCredentialProvider;

/**
 * This class provides a default implementation to resolve the login credentials of the scheduler user. This class only
 * supports anonymous users.
 */
public class DefaultSchedulerCredentialProvider implements SchedulerCredentialProvider {
  /**
   * Method returns the login name of the scheduler user.
   * 
   * @return {@link String} Login name of the user. The method may return null.
   */
  @Override
  public String getSchedulerUser( ) {
    return null;
  }

  /**
   * Unencrypted password of the scheduler user.
   * 
   * @return {@link String} unencrypted password of the scheduler user. The method may return null.
   */
  @Override
  public String getSchedulerPassword( ) {
    return null;
  }

  /**
   * Method returns true if the scheduler user is an anonymous user.
   * 
   * @return boolean Method returns true if the scheduler user is an anonymous user.
   */
  @Override
  public boolean isAnonymous( ) {
    return true;
  }
}
