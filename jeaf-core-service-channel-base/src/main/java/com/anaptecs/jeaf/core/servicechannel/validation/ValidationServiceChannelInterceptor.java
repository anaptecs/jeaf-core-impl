/**
 * Copyright 2004 - 2018 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.validation;

import java.lang.reflect.Method;

import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.spi.ServiceCall;
import com.anaptecs.jeaf.core.spi.ServiceChannelInterceptor;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.tools.api.validation.ValidationTools;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.errorhandling.ApplicationException;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;

/**
 * Class implements a service channel interceptor that is used to ensure that Java Validation constraints are fulfilled
 * on requests and responses.
 * 
 * @author JEAF Development Team
 * @version JEAF Release 1.3
 */
public class ValidationServiceChannelInterceptor implements ServiceChannelInterceptor {

  /**
   * Through this method an interceptor will be notified, that the service described by the passed ServiceCall object
   * will be called. This method will be invoked before the actual service call.
   * 
   * This method will validate the constraints on the request parameters for the service call.
   * 
   * @param pServiceCall Object describing the service call. The parameter is never null.
   */
  @Override
  public void preServiceCall( ServiceCall pServiceCall ) {
    // Check if request validation is required.
    Method lServiceMethod = pServiceCall.getTargetMethod();
    ValidationTools lValidationTools = Tools.getValidationTools();
    boolean lRequiresRequestValidation = lValidationTools.requiresRequestValidation(lServiceMethod);

    // Request validation required
    if (lRequiresRequestValidation == true) {
      XFun.getTrace().debug("Starting request validation.");

      // Validate parameters
      Service lTargetService = pServiceCall.getTargetService();
      lValidationTools.enforceParameterValidation(lTargetService, lServiceMethod, pServiceCall.getParameters());
    }
    // No request validation required
    else {
      XFun.getTrace().debug("No request validation required.");
    }
  }

  /**
   * Through this method an interceptor will be notified, that the service described by the passed ServiceCall object
   * was called successfully. This method will be invoked after the actual service call completed without any exception.
   * 
   * This method will validate the constraints on the response of the service call.
   * 
   * @param pServiceCall Object describing the service call. The parameter is never null.
   * @param pResult Result of the service call. Parameter may be null in case that method does not have a return value
   * or null is returned by the service.
   */
  @Override
  public void postServiceCall( ServiceCall pServiceCall, Object pResult ) {
    // Check if response validation is required.
    Method lServiceMethod = pServiceCall.getTargetMethod();
    ValidationTools lValidationTools = Tools.getValidationTools();
    boolean lRequiresResponseValidation = lValidationTools.requiresResponseValidation(lServiceMethod);

    // Response validation required
    if (lRequiresResponseValidation == true) {
      XFun.getTrace().debug("Starting response validation.");

      // Check constraints for response.
      Service lTargetService = pServiceCall.getTargetService();
      lValidationTools.enforceReturnValueValidation(lTargetService, lServiceMethod, pResult);
    }
    // No response validation required
    else {
      XFun.getTrace().debug("No response validation required.");
    }
  }

  /**
   * Through this method an interceptor will be notified, that the service described by the passed ServiceCall object
   * was called and an exception occurred. This method will be invoked after the actual service call completed with a
   * runtime exception.
   * 
   * @param pServiceCall Object describing the service call. The parameter is never null.
   * @param pRuntimeException Runtime exception that occurred during the service call. The parameter is never null.
   */
  @Override
  public void postServiceCall( ServiceCall pServiceCall, RuntimeException pRuntimeException ) {
    // Nothing to do.
  }

  /**
   * Through this method an interceptor will be notified, that the service described by the passed ServiceCall object
   * was called and an error occurred. This method will be invoked after the actual service call completed with an
   * error.
   * 
   * @param pServiceCall Object describing the service call. The parameter is never null.
   * @param pError Error object that occurred during the service call. The parameter is never null.
   */
  @Override
  public void postServiceCall( ServiceCall pServiceCall, Error pError ) {
    // Nothing to do.
  }

  /**
   * Through this method an interceptor will be notified, that the service described by the passed ServiceCall object
   * was called and an exception occurred. This method will be invoked after the actual service call completed with an
   * application exception.
   * 
   * @param pServiceCall Object describing the service call. The parameter is never null.
   * @param pApplicationException Application exception that occurred during the service call. The parameter is never
   * null.
   */
  @Override
  public void postServiceCall( ServiceCall pServiceCall, ApplicationException pApplicationException ) {
    // Nothing to do.
  }

  /**
   * Through this method an interceptor will be notified, that the service described by the passed ServiceCall object
   * was called and an exception occurred. This method will be invoked after the actual service call completed with a
   * system exception.
   * 
   * @param pServiceCall Object describing the service call. The parameter is never null.
   * @param pSystemException System exception that occurred during the service call. The parameter is never null
   */
  @Override
  public void postServiceCall( ServiceCall pServiceCall, SystemException pSystemException ) {
    // Nothing to do.
  }
}
