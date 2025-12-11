/**
 * Copyright 2004 - 2020 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.junit.interceptors;

import com.anaptecs.jeaf.core.spi.ServiceCall;
import com.anaptecs.jeaf.core.spi.ServiceChannelInterceptor;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;

public class MyTestInterceptor implements ServiceChannelInterceptor {

  @Override
  public void preServiceCall( ServiceCall pServiceCall ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postServiceCall( ServiceCall pServiceCall, Object pResult ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postServiceCall( ServiceCall pServiceCall, RuntimeException pRuntimeException ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postServiceCall( ServiceCall pServiceCall, Error pError ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postServiceCall( ServiceCall pServiceCall, ApplicationException pApplicationException ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postServiceCall( ServiceCall pServiceCall, SystemException pSystemException ) {
    // TODO Auto-generated method stub

  }

}
