/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.jee.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.servicechannel.JEAFCore;
import com.anaptecs.jeaf.xfun.api.XFun;

/**
 * Filter is used to provide session scoped information via JEAF Core Session Context Manager
 * 
 * @version JEAF Release 1.3
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class SessionContextServletFilter implements Filter {

  @Override
  public void destroy( ) {
    // Nothing to do.
  }

  @Override
  public void doFilter( ServletRequest pRequest, ServletResponse pResponse, FilterChain pFilterChain )
    throws IOException, ServletException {

    // Filter only works for http requests.
    if (pRequest instanceof HttpServletRequest) {

      // Free current thread from may be existing associations to other sessions. Actually this should not be required
      // here. However as it may cause heavy security issues we will ensure once again that all associations from the
      // session context manager are removed before we start to handle the current request.
      //
      // As you can see below the association are actually removed in the finally block.
      WebSessionContextManager lSessionContextManager = (WebSessionContextManager) JEAFCore.getInstance()
          .getLifecycleManager().getContextManager().getSessionContextManager();
      lSessionContextManager.releaseCurrentAssociationsToCurrentThread();

      try {
        // Lookup http session of current user.
        HttpServletRequest lHttpServletRequest = (HttpServletRequest) pRequest;
        HttpSession lHttpSession = lHttpServletRequest.getSession();

        // Trace attributes of http session.
        if (MessageConstants.HTTP_SESSION_CONTENT.isEnabled() == true) {
          XFun.getTrace().write(MessageConstants.HTTP_SESSION_CONTENT, lHttpSession.getId());
          Enumeration<?> lAttributeNames = lHttpSession.getAttributeNames();
          if (lAttributeNames.hasMoreElements() == true) {
            while (lAttributeNames.hasMoreElements() == true) {
              String lAttributeName = lAttributeNames.nextElement().toString();
              StringBuilder lContent = new StringBuilder();
              lContent.append(lAttributeName);
              lContent.append(": ");
              lContent.append(lHttpSession.getAttribute(lAttributeName));
              XFun.getTrace().write(MessageConstants.HTTP_SESSION_ATTRIBUTE, lContent.toString());
            }
          }
          else {
            XFun.getTrace().write(MessageConstants.HTTP_SESSION_ATTRIBUTE, "HTTP Session has no attributes");
          }
        }

        // Pass current http session to web session context manager
        lSessionContextManager.setHttpSession(lHttpSession, lHttpServletRequest.getUserPrincipal());

        // Delegate request to the rest of the filter chain.
        pFilterChain.doFilter(pRequest, pResponse);
      }

      // No matter what happens we have to release the associations of the context manager to the current thread.
      finally {
        lSessionContextManager.releaseCurrentAssociationsToCurrentThread();
      }
    }
    // Only delegate request to the rest of the filter chain
    else {
      XFun.getTrace().write(MessageConstants.IGNORING_SERVLET_REQUEST);
      pFilterChain.doFilter(pRequest, pResponse);
    }
  }

  @Override
  public void init( FilterConfig pFilterConfig ) throws ServletException {
    // Nothing to do.
  }
}
