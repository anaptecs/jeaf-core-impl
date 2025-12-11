/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.jee.servlet;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.core.servicechannel.api.SessionContextManager;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.messages.MessageID;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This class implements a session context manager that uses the http session to store the session context. Thus this
 * class can only be used within environments that provide a http session.
 * 
 * @author Tillmann Schall (TLS)
 * @version JEAF Release 1.2
 */
public class WebSessionContextManager implements SessionContextManager {
  /**
   * Name of the key under which the JEAF session context is stored within the http session.
   */
  public static final String JEAF_SESSION_CONTEXT_KEY = SessionContext.class.getName();

  /**
   * Name of key which is used to mark a session context that is was created without an existing http session.
   */
  public static final String FAKE_CONTEXT_KEY = "FAKE_CONTEXT";

  /**
   * Map contains the thread to HTTP session mapping. Through this map we can find the HTTP session that is associated
   * with a thread.
   */
  private final Map<Thread, HttpSession> threadToSessionMapping = new HashMap<Thread, HttpSession>();

  /**
   * Map contains the session id to thread mapping. This is needed in order to be able to remove the association between
   * threads and sessions.
   */
  private final Map<String, List<Thread>> sessionIdToThreadMapping = new HashMap<String, List<Thread>>();

  /**
   * Map contains the thread to principal mapping. This is required in order to find out for which user principal the
   * current thread is executed.
   */
  private final Map<Thread, Principal> threadToUserPrincipalMapping = new HashMap<Thread, Principal>();

  /**
   * Map contains all session context objects that were registered by this session context manager.
   */
  private final Map<Principal, SessionContext> registeredSessionContexts = new HashMap<Principal, SessionContext>();

  private final Map<Thread, SessionContext> fakeSessionContextToThreadMapping = new HashMap<Thread, SessionContext>();

  /**
   * Method returns the session context of the current user. This session manager implementation uses therefore from a
   * thread local storage in combination with a servlet filter.
   * 
   * @return {@link SessionContext} Session context of the current user. The method never returns null.
   */
  @Override
  public synchronized SessionContext getSessionContext( ) {
    SessionContext lSessionContext;
    Thread lCurrentThread = Thread.currentThread();

    // Get session context from http session of current user
    HttpSession lHttpSession = this.getHttpSessionForCurrentThread();

    if (lHttpSession != null) {
      // Do what we actually want to do.
      lSessionContext = this.getSessionContextFromHttpSession(lHttpSession);

      // Session context does not exist yet for the current user inside the http session so it either is registered or
      // we have to create a new one.
      if (lSessionContext == null) {
        // Check if a session context was registered for the current user.
        Principal lCurrentUser = threadToUserPrincipalMapping.get(lCurrentThread);
        lSessionContext = registeredSessionContexts.get(lCurrentUser);

        // Session context was already registered before.
        if (lSessionContext != null) {
          // If the found session is not a fake one, we will add it so the http session.
          if (this.isFakeSessionContext(lSessionContext) == false) {
            lHttpSession.setAttribute(JEAF_SESSION_CONTEXT_KEY, lSessionContext);
            registeredSessionContexts.remove(lCurrentUser);

            SessionContext lRemovedFakeSession = fakeSessionContextToThreadMapping.remove(lCurrentThread);
            if (lRemovedFakeSession != null) {
              XFun.getTrace().write(MessageConstants.FOUND_UNEXPECTED_FAKE_SESSION, lRemovedFakeSession.toString());
            }

            // Write trace message.
            XFun.getTrace().write(MessageConstants.REMOVED_SESSION_CONTEXT_FROM_TEMP_STORE, lCurrentUser.getName(),
                lSessionContext.toString(), lHttpSession.getId());
          }
        }
        // Create session context and add it to the users http session.
        else {
          lSessionContext = new SessionContext();
          lHttpSession.setAttribute(JEAF_SESSION_CONTEXT_KEY, lSessionContext);
        }
      }
    }
    // Create dummy session context for calls that do not have a session context. This may be during initialization e.g.
    // The context will be moved to the http session as soon as possible.
    else {
      // Fake session context was already created for this thread.
      if (fakeSessionContextToThreadMapping.containsKey(lCurrentThread) == true) {
        lSessionContext = fakeSessionContextToThreadMapping.get(lCurrentThread);
      }
      else {
        lSessionContext = new SessionContext();
        lSessionContext.addContextObject(FAKE_CONTEXT_KEY, "HTTP session unknown");
        fakeSessionContextToThreadMapping.put(lCurrentThread, lSessionContext);
      }
    }
    return lSessionContext;
  }

  /**
   * Method returns if a session context is available for the current thread.
   * 
   * @return boolean Method returns true if a session context is available. If currently no session context exists the
   * method returns false.
   */
  public boolean isSessionContextAvailable( ) {
    boolean lSessionContextAvailable;

    // Get current http session.
    HttpSession lCurrentHttpSession = this.getHttpSessionForCurrentThread();

    if (lCurrentHttpSession != null) {
      // Try to get session context from current http session
      SessionContext lCurrentSessionContext = this.getSessionContextFromHttpSession(lCurrentHttpSession);
      if (lCurrentSessionContext != null) {
        lSessionContextAvailable = true;
      }
      else {
        lSessionContextAvailable = false;
      }
    }
    // No http session available
    else {
      lSessionContextAvailable = false;
    }

    // Return result of session context check.
    return lSessionContextAvailable;
  }

  /**
   * Method returns the session context that is stored in the passed http session.
   * 
   * @param pHttpSession Http session from which the session context should be read. The parameter must not be null.
   * @return {@link SessionContext} of the http session or null if none is stored there.
   */
  private SessionContext getSessionContextFromHttpSession( HttpSession pHttpSession ) {
    // Check parameter.
    Assert.assertNotNull(pHttpSession, "pHttpSession");

    return (SessionContext) pHttpSession.getAttribute(JEAF_SESSION_CONTEXT_KEY);
  }

  /**
   * Method returns the HTTP session for the current thread.
   * 
   * @return {@link HttpSession} HTTP session that is associated with the current thread. The method returns null if no
   * HTTP session is associated with the current thread. The method ensures that the returned HTTP session is still
   * valid and returns null if the associated session may be invalid.
   */
  synchronized HttpSession getHttpSessionForCurrentThread( ) {
    // Get current thread and return the associated HTTP session.
    Thread lCurrentThread = Thread.currentThread();
    HttpSession lHttpSession = threadToSessionMapping.get(lCurrentThread);

    // Check if session is still alive.
    if (lHttpSession != null) {
      try {
        lHttpSession.getLastAccessedTime();
      }
      // As the JEE standard does not provide an other way to find out if a HTTP session is still valid we have to try
      // if an exception occurs.
      catch (IllegalStateException e) {
        XFun.getTrace().write(MessageConstants.IGNORING_INVALID_HTTP_SESSION, lHttpSession.getId());
        // As the session is invalid we have to cleanup our internal structures.
        this.httpSessionInvalidated(lHttpSession);

        // As the session is invalid we will return null in this case.
        lHttpSession = null;
      }
    }

    return lHttpSession;
  }

  /**
   * Method associates the passed http session with the current thread.
   * 
   * @param pHttpSession HTTP session that should be associated with the current thread. The parameter must not be null.
   * @param pCurrentUser Principal of the user to which the passed http session is assigned. The parameter may be null.
   */
  synchronized void setHttpSession( HttpSession pHttpSession, Principal pCurrentUser ) {
    // Check parameter
    Assert.assertNotNull(pHttpSession, "pSessionContext");

    // Cleanup may be existing associations between the current thread and other sessions. Therefore we need the thread
    // list of the previously associated session.
    Thread lCurrentThread = Thread.currentThread();
    HttpSession lPreviousHttpSession = threadToSessionMapping.get(lCurrentThread);

    // Thread was already associated to a session.
    if (lPreviousHttpSession != null) {
      List<Thread> lPreviousThreadList = sessionIdToThreadMapping.get(lPreviousHttpSession.getId());
      boolean lRemoved = lPreviousThreadList.remove(lCurrentThread);
      if (lRemoved == true) {
        // Write trace message
        MessageID lMessageID = MessageConstants.REMOVED_HTTP_SESSION_THREAD_ASSOCIATION;
        XFun.getTrace().write(lMessageID, lPreviousHttpSession.getId(), lCurrentThread.getName());
      }
    }

    // Check if session is already known
    String lSessionID = pHttpSession.getId();
    List<Thread> lThreads = sessionIdToThreadMapping.get(lSessionID);
    if (lThreads == null) {
      lThreads = new ArrayList<Thread>();
      sessionIdToThreadMapping.put(lSessionID, lThreads);
    }

    // Add current thread to the list of threads.
    lThreads.add(lCurrentThread);

    // Associated passed HTTP session with current thread.
    threadToSessionMapping.put(lCurrentThread, pHttpSession);

    // Also associate the passed principal with the current thread.
    threadToUserPrincipalMapping.put(lCurrentThread, pCurrentUser);

    // Write trace message
    MessageID lMessageID = MessageConstants.ASSOCIATED_HTTP_SESSION_WITH_CURRENT_THREAD;
    String lUserName;
    if (pCurrentUser != null) {
      lUserName = pCurrentUser.getName();
    }
    else {
      lUserName = "anonymous";
    }
    XFun.getTrace().write(lMessageID, pHttpSession.getId(), lUserName, lCurrentThread.getName());
  }

  /**
   * Method is used to register a session context for a specific users. The method is required in cases where a session
   * will be created outside from a http session and its content should be available for the passed user as soon as the
   * session is available. This situation e.g. occurs when an application uses http basic authentication.
   * 
   * @param pSessionContext SessionContext that should be registered for the passed user.
   * @param pUser Principal object representing the user for which the session context should be registered.
   */
  public synchronized void registerSessionContext( SessionContext pSessionContext, Principal pUser ) {
    // Check parameters
    Check.checkInvalidParameterNull(pSessionContext, "pSessionContext");
    Check.checkInvalidParameterNull(pUser, "pUser");

    // Write trace
    Trace lTrace = XFun.getTrace();
    String lUserName = pUser.getName();
    lTrace.write(MessageConstants.RECEIVED_SESSION_CONTEXT, lUserName, pSessionContext.toString());

    // Removed association between current thread and session context.
    fakeSessionContextToThreadMapping.remove(Thread.currentThread());

    // Check if session context already exists for the passed user.
    SessionContext lExistingSessionContext = registeredSessionContexts.get(pUser);
    if (lExistingSessionContext == null) {
      registeredSessionContexts.put(pUser, pSessionContext);
      lTrace.write(MessageConstants.NEW_SESSION_CONTEXT, lUserName, pSessionContext.toString());
    }
    // We have to merge session context objects. There are some race conditions that cause a situation where more than
    // one session context will be created.
    else {
      lTrace.write(MessageConstants.MERGING_SESSION_CONTEXTS, lUserName);

      // Merge existing session context with the passed new one.
      Set<String> lNewKeys = pSessionContext.getKeys();
      for (String lNextKey : lNewKeys) {

        Object lNextEntryObject = pSessionContext.getContextObject(lNextKey);

        // Replace existing session context entry.
        if (lExistingSessionContext.containsContextObject(lNextKey) == true) {
          Object lOldEntry = lExistingSessionContext.getContextObject(lNextKey);

          // Check if new value and existing entry are the same.
          if (lOldEntry.equals(lNextEntryObject) == false) {
            lExistingSessionContext.removeContextObject(lNextKey);
            lExistingSessionContext.addContextObject(lNextKey, lNextEntryObject);

            // Write trace
            lTrace.write(MessageConstants.REPLACING_SESSION_CONTEXT_ENTRY, lUserName, lNextKey,
                lNextEntryObject.toString(), lOldEntry.toString());
          }
        }
        // Add new entry to session context.
        else {
          lExistingSessionContext.addContextObject(lNextKey, lNextEntryObject);

          // Write trace
          lTrace.write(MessageConstants.ADDING_SESSION_CONTEXT_ENTRY, lUserName, lNextKey, lNextEntryObject.toString());
        }
      }
    }
  }

  /**
   * MEthod checks if the passed session context is a fake session context. Fake session contexts can be identified by a
   * special key {@link #FAKE_CONTEXT_KEY}.
   * 
   * @param pSessionContext Context that should be checked whether it is a fake one or not. The parameter must not be
   * null.
   * @return boolean Method returns true if the passed session context is a fake context and true in all other cases.
   */
  boolean isFakeSessionContext( SessionContext pSessionContext ) {
    boolean lContainsFakeKey = pSessionContext.containsContextObject(FAKE_CONTEXT_KEY);
    int lContextSize = pSessionContext.getKeys().size();
    boolean lIsFakeContext;
    if (lContainsFakeKey == true && lContextSize == 1) {
      lIsFakeContext = true;
    }
    else {
      lIsFakeContext = false;
    }
    return lIsFakeContext;
  }

  /**
   * Method is intended to be called whenever a HTTP session is invalidated (not matter for what reason) in order to
   * cleanup internal references between sessions and threads.
   * 
   * @param pHttpSession HTTP session that was invalidated. The parameter must not be null.
   */
  synchronized void httpSessionInvalidated( HttpSession pHttpSession ) {
    // Check parameter
    Assert.assertNotNull(pHttpSession, "pHttpSession");

    // Cleanup associations to passed HTTP session
    String lSessionID = pHttpSession.getId();
    List<Thread> lThreadList = sessionIdToThreadMapping.get(lSessionID);
    sessionIdToThreadMapping.remove(lSessionID);

    // Found existing association to invalidated http session, thus we need to cleanup.
    if (lThreadList != null) {
      for (Thread lNextThread : lThreadList) {
        // Release thread to session mapping.
        threadToSessionMapping.remove(lNextThread);

        // Write trace message
        MessageID lMessageID = MessageConstants.REMOVED_HTTP_SESSION_THREAD_ASSOCIATION;
        XFun.getTrace().write(lMessageID, lSessionID, lNextThread.getName());
      }
    }
  }

  /**
   * Method releases all associations of the web context session manager to the current thread. This method has to be
   * called by the session context filter to ensure that sessions are isolated per user
   * 
   * @see https://development.anaptecs.de/jira/browse/JEAF-841
   */
  synchronized void releaseCurrentAssociationsToCurrentThread( ) {
    Thread lCurrentThread = Thread.currentThread();
    Trace lTrace = XFun.getTrace();

    // Release association to http session
    HttpSession lHttpSession = threadToSessionMapping.remove(lCurrentThread);
    if (lHttpSession != null) {
      lTrace.write(MessageConstants.RELEASED_HTTP_SESSION_FROM_THREAD, lHttpSession.getId());
    }

    // Release association to principal.
    Principal lPrincipal = threadToUserPrincipalMapping.remove(lCurrentThread);
    if (lPrincipal != null) {
      lTrace.write(MessageConstants.RELEASED_USER_PRICIPAL_FROM_THREAD, lPrincipal.getName());
    }

    // Release association to fake session.
    SessionContext lFakeSession = fakeSessionContextToThreadMapping.remove(lCurrentThread);
    if (lFakeSession != null) {
      lTrace.write(MessageConstants.RELEASED_FAKE_SESSION_CONTEXT_FROM_THREAD, lFakeSession.toString());
    }
  }
}
