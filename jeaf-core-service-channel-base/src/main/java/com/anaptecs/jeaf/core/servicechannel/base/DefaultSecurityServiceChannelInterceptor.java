/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import com.anaptecs.jeaf.core.spi.ServiceCall;
import com.anaptecs.jeaf.core.spi.ServiceChannelInterceptor;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;

/**
 * Class provides a default implementation of a security channel interceptor. However this interceptor does not ensure
 * that a service is only called if the calling user has the sufficient rights. That's why this implementation should
 * only be used for testing or if security does not matter.
 * 
 * @author JEAF Development Team
 * @version JEAF Release 1.2
 */
public class DefaultSecurityServiceChannelInterceptor implements ServiceChannelInterceptor {

  public void preServiceCall( ServiceCall pServiceCall ) {
    // Nothing to do.
  }

  public void postServiceCall( ServiceCall pServiceCall, Object pResult ) {
    // Nothing to do.
  }

  public void postServiceCall( ServiceCall pServiceCall, RuntimeException pRuntimeException ) {
    // Nothing to do.
  }

  public void postServiceCall( ServiceCall pServiceCall, Error pError ) {
    // Nothing to do.
  }

  public void postServiceCall( ServiceCall pServiceCall, ApplicationException pApplicationException ) {
    // Nothing to do.
  }

  public void postServiceCall( ServiceCall pServiceCall, SystemException pSystemException ) {
    // Nothing to do.
  }
}
