/**
 * Copyright 2004 - 2013 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.jee.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.errorhandling.ErrorCode;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This class implements a simple servlet that can be used to perform a logout from a web application
 * 
 * @author JEAF Development Team
 * @version JEAF Release 1.3
 */
public class LogoutServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet( HttpServletRequest pRequest, HttpServletResponse pResponse )
    throws ServletException, IOException {

    // Add some trace statements
    Trace lTrace = XFun.getTrace();
    lTrace.info("Performing logout");

    try {

      // Get existing http session but do not create one.
      HttpSession lHttpSession = pRequest.getSession(false);

      PrintWriter lWriter = pResponse.getWriter();
      lWriter.append("Performing logout\n");

      if (lHttpSession != null) {
        lHttpSession.invalidate();
        String lMessage = "Invalidated session '" + lHttpSession.getId();
        lTrace.info(lMessage);
        lWriter.append(lMessage + "'\n");
      }
      else {
        String lMessage = "No session exists thus we have nothing to do.";
        lTrace.info(lMessage);
        lWriter.append(lMessage + "\n");
      }
      lTrace.info("Logout completed.");
      lWriter.write("Logout completed.\n");
      pResponse.setStatus(HttpServletResponse.SC_OK);
      lWriter.close();
    }
    catch (RuntimeException e) {
      ErrorCode lErrorCode = MessageConstants.EXCEPTION_DURING_LOGOUT;
      lTrace.error(lErrorCode, e);
      pResponse.getWriter().write(lErrorCode.toString());
      pResponse.getWriter().write(e.getMessage());
      pResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
