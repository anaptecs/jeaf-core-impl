/**
 * Copyright 2004 - 2013 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.jee.servlet;

import java.util.Enumeration;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.servicechannel.JEAFCore;
import com.anaptecs.jeaf.xfun.api.XFun;

/**
 * Class is intended for debugging of http session issues. This class only produces traces messages whenever a http
 * session is created or destroyed.
 * 
 * @author JEAF Development Team
 * @version JEAF Release 1.3
 */
@WebListener
public class JEAFWebSessionListener implements HttpSessionListener {

  /**
   * Method writes trace message that a new http session was created.
   */
  @Override
  public void sessionCreated( HttpSessionEvent pSessionEvent ) {
    HttpSession lSession = pSessionEvent.getSession();
    XFun.getTrace().write(MessageConstants.HTTP_SESSION_CREATED, lSession.getId());
    Enumeration<?> lAttributeNames = lSession.getAttributeNames();
    while (lAttributeNames.hasMoreElements() == true) {
      String lAttributeName = lAttributeNames.nextElement().toString();
      XFun.getTrace().info(lAttributeName + ": " + lSession.getAttribute(lAttributeName));
    }
  }

  /**
   * Method writes trace message that a http session was destroyed.
   */
  @Override
  public void sessionDestroyed( HttpSessionEvent pSessionEvent ) {
    XFun.getTrace().write(MessageConstants.HTTP_SESSION_DESTROYED, pSessionEvent.getSession().getId());
    WebSessionContextManager lSessionManager = (WebSessionContextManager) JEAFCore.getInstance().getLifecycleManager()
        .getContextManager().getSessionContextManager();
    lSessionManager.httpSessionInvalidated(pSessionEvent.getSession());
  }
}
