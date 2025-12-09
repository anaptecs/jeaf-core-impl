/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.performance;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.anaptecs.jeaf.core.annotations.JEAFService;
import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.junit.core.GeneratorTestService;
import com.anaptecs.jeaf.junit.core.ValidationTestObject;
import com.anaptecs.jeaf.junit.core.ValidationTestService;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.tools.api.performance.Stopwatch;
import com.anaptecs.jeaf.tools.api.performance.TimePrecision;

/**
 * Class integrates all test suites of this JUnit project to one suite that can be used to run all tests.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
@Disabled
public class JEAFPerformanceTest {
  @JEAFService
  private GeneratorTestService generatorTestService;

  @JEAFService
  private ValidationTestService validationTestService;

  private static final int INVOCATIONS = 2000000;

  /**
   * Initialize test case. Therefore the name of the test that should be executed must be provided.
   * 
   * @param pName Name of the test method that should be executed by this test. The Parameter must not be null.
   */
  public JEAFPerformanceTest( ) {
    generatorTestService = JEAF.getService(GeneratorTestService.class);
    validationTestService = JEAF.getService(ValidationTestService.class);
  }

  @Test
  public void testLocalMethodCallPerformance( ) {
    Stopwatch lStopwatch =
        Tools.getPerformanceTools().createStopwatch("Local method call performance", TimePrecision.NANOS);
    lStopwatch.start(INVOCATIONS);
    for (int i = 0; i < INVOCATIONS; i++) {
      this.doNothing();
    }
    lStopwatch.stopAndTrace();
  }

  @Test
  public void testServiceChannelPerformance( ) {
    Double lDouble = 1.23456;

    Stopwatch lStopwatch =
        Tools.getPerformanceTools().createStopwatch("JEAF Service channel performance", TimePrecision.NANOS);
    lStopwatch.start(INVOCATIONS);
    for (int i = 0; i < INVOCATIONS; i++) {
      generatorTestService.doWhatIMean(lDouble);
    }
    lStopwatch.stopAndTrace();
  }

  @Test
  public void testServiceChannelWithValidationPerformance( ) {
    // Create object that will be passed to all requests
    ValidationTestObject lValidationTestObject = ValidationTestObject.Builder.newBuilder().build();
    lValidationTestObject.setMyEMail("weeasy@anaptecs.de");
    lValidationTestObject.setDateOfBirth(null);

    Stopwatch lStopwatch = Tools.getPerformanceTools()
        .createStopwatch("JEAF Service channel with validation performance", TimePrecision.NANOS);
    lStopwatch.start(INVOCATIONS);
    for (int i = 0; i < INVOCATIONS; i++) {
      validationTestService.createValidationTestObject(null);
    }
    lStopwatch.stopAndTrace();
  }

  @Test
  public void tesReflectionPerformance( ) throws Exception {
    Method lMethod = this.getClass().getMethod("doNothing");
    Stopwatch lStopwatch =
        Tools.getPerformanceTools().createStopwatch("Reflection call performance", TimePrecision.NANOS);
    lStopwatch.start(INVOCATIONS);
    for (int i = 0; i < INVOCATIONS; i++) {
      lMethod.invoke(this);
    }
    lStopwatch.stopAndTrace();
  }

  public void doNothing( ) {
  }
}