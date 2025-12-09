/**
 * Copyright 2004 - 2022 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.init;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.JEAFSplash;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.jee.commons.EJBHelper;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJBLocal;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.config.Configuration;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;
import com.anaptecs.jeaf.xfun.api.info.RuntimeEnvironment;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This servlet is used to initialize the JEAF framework. Due to this fact it is not a real servlet but a startup hook
 * for an application server. For that reason it does not handle any http requests and will only be loaded at startup.
 */
@WebServlet(displayName = "JEAF Core EJB Container Init Servlet", loadOnStartup = 1)
public class EJBContainerInitServlet extends HttpServlet {

  /**
   * Serial version UID as defined by Java's serialization mechanism.
   */
  private static final long serialVersionUID = 1;

  /**
   * Initialize object.
   */
  public EJBContainerInitServlet( ) {
    // Nothing to do.
  }

  /**
   * Method will be called by the servlet container when the application server starts up to initialize all servlets.
   * Thereby the servlet calls the lifecycle manager bean in order to initialize the JEAF framework.
   * 
   * @param pConfig Initialization parameters for the servlet. The parameter won't be used.
   * @throws ServletException if an exception occurs during the initialization of the JEAF framework.
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public final void init( ServletConfig pConfig ) throws ServletException {
    super.init(pConfig);

    try {
      // Determine environment in which we are running
      RuntimeEnvironment lRuntimeEnvironment = XFun.getInfoProvider().getRuntimeEnvironment();
      Trace lTrace = XFun.getTrace();
      lTrace.info(MessageConstants.STARTING_JEAF_INIT, lRuntimeEnvironment.name());

      // Run environment specific initialization
      switch (lRuntimeEnvironment) {
        case WEB_CONTAINER:
          // Perform local JEAF initialization (inside web container)
          JEAF.load();
          break;

        case EJB_CONTAINER:
          JEAFSplash.printSplashScreen();
          // Sleep until deployment of EJBs is really completed.
          try {
            Configuration lConfiguration = XFun.getConfigurationProvider().getSystemPropertiesConfiguration();
            Long lDelay = lConfiguration.getConfigurationValue("delayJEAFInitialization", 0L, Long.class);
            if (lDelay > 0) {
              lTrace.warn(MessageConstants.DELAYING_JEAF_INIT, lDelay.toString());
              Thread.sleep(lDelay);
            }
          }
          catch (InterruptedException e) {
            // Nothing to do
          }

          // Perform lookup for LifecycleManagerEJB and perform initialization.
          String lJNDIName = LifecycleManagerEJBLocal.LOCAL_EJB_JNDI_NAME;
          lTrace.info(MessageConstants.LOOKUP_LIFECYCLE_MANAGER, lJNDIName);

          // Perform lookup for lifecycle manager and initialize it.
          LifecycleManagerEJBLocal lEJBLifecycleManager =
              EJBHelper.lookupEJB(lJNDIName, LifecycleManagerEJBLocal.class);
          lEJBLifecycleManager.initialize();
          break;

        // Unsupported runtime environment
        default:
          throw new JEAFSystemException(MessageConstants.UNSUPPORTED_RUNTIME_ENVIRONMENT, lRuntimeEnvironment.name());
      }
    }
    // Handle all exceptions and rethrow them as ServletException as defined by the Servlet API.
    catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
