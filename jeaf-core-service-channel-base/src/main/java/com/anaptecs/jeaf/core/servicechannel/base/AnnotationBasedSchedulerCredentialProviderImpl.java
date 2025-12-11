/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.annotations.SchedulerCredentials;
import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.servicechannel.api.SchedulerCredentialProvider;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.xfun.api.config.ConfigurationReader;

public class AnnotationBasedSchedulerCredentialProviderImpl implements SchedulerCredentialProvider {
  private final String user;

  private final String password;

  private boolean anonymous;

  public AnnotationBasedSchedulerCredentialProviderImpl( ) {
    ConfigurationReader lReader = new ConfigurationReader();
    Class<?> lClass =
        lReader.readClassFromConfigFile(SchedulerCredentials.SCHEDULER_CREDENTIALS_RESOURCE_NAME, JEAF.CORE_BASE_PATH);
    SchedulerCredentials lAnnotation = lClass.getAnnotation(SchedulerCredentials.class);
    String lUser = lAnnotation.user();
    if (Tools.getStringTools().isRealString(lUser)) {
      user = lUser.trim();
      anonymous = false;
    }
    else {
      user = null;
      anonymous = true;
    }
    password = lAnnotation.password();
  }

  @Override
  public String getSchedulerUser( ) {
    return user;
  }

  @Override
  public String getSchedulerPassword( ) {
    return password;
  }

  @Override
  public boolean isAnonymous( ) {
    return anonymous;
  }
}
