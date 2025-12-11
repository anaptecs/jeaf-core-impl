/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jse;

import java.util.Timer;
import java.util.TimerTask;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.servicechannel.base.GenericLifecycleManager;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.messages.MessageID;

/**
 * Time task can be used to request the notification of all triggers.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public class JSETimer extends TimerTask {
  /**
   * Timer object.
   */
  private final Timer timer;

  /**
   * LifecycleManager that will be used. The reference is never null.
   */
  private final GenericLifecycleManager lifecycleManager;

  /**
   * Initialize object. Therefore the used life cycle manager has to be passed.
   * 
   * @param pLifecycleManager LifecycleManager to use. The parameter must not be null.
   */
  public JSETimer( GenericLifecycleManager pLifecycleManager ) {
    // Check parameter.
    Check.checkInvalidParameterNull(pLifecycleManager, "pLifecycleManager");

    lifecycleManager = pLifecycleManager;
    timer = new Timer("JEAF Timer", true);
  }

  /**
   * Run method requests life cycle manager to notify all registered triggers.
   */
  @Override
  public void run( ) {
    lifecycleManager.fireTriggers();
  }

  /**
   * Method starts the timer with the passed interval.
   * 
   * @param pInterval Scheduling interval in seconds that should be used.
   * @param pDelay Delay in seconds until the scheduling starts the first time.
   */
  public void start( int pInterval, int pDelay ) {
    // Write trace message and start scheduling.
    final MessageID lMessageID = MessageConstants.STARTING_SCHEDULING;
    XFun.getTrace().write(lMessageID, String.valueOf(pInterval));

    // Start timer.
    timer.schedule(this, pDelay * 1000L, pInterval * 1000L);
  }

  /**
   * Method stops the timer.
   */
  public void stop( ) {
    timer.cancel();
    timer.purge();
    XFun.getTrace().write(MessageConstants.STOPPED_SCHEDULING);
  }
}
