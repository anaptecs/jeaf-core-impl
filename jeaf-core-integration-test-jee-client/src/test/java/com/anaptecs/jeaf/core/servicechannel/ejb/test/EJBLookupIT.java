/**
 * Copyright 2004 - 2020 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.jee.commons.EJBHelper;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProxy;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJB;
import com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJBHome;
import com.anaptecs.jeaf.junit.core.GeneratorTestService;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.errorhandling.SystemException;
import com.anaptecs.jeaf.xfun.api.info.InfoProvider;
import com.anaptecs.jeaf.xfun.api.info.JavaRelease;
import com.anaptecs.jeaf.xfun.api.messages.MessageRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.wildfly.naming.client.WildFlyInitialContextFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EJBLookupIT {
  private static final JavaRelease JAVA_VERSION =
      InfoProvider.getInfoProvider().getJavaRuntimeEnvironment().getJavaRelease();

  @Test
  @Order(10)
  public void lookupEJBs( ) throws Exception {
    if (JAVA_VERSION.isEqualOrLower(JavaRelease.JAVA_11)) {
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, WildFlyInitialContextFactory.class.getName());
      System.setProperty(Context.PROVIDER_URL, "http-remoting://localhost:9080");

      InitialContext lContext = new InitialContext();
      String lEJBJNDI =
          "ejb:jeaf-core-integration-test-jee-ear-${project.version}/jeaf-core-service-channel-ejb/LifecycleManagerEJB!com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJB";
      lEJBJNDI = XFun.getConfigurationProvider().replaceSystemProperties(lEJBJNDI);
      Object lLookup = lContext.lookup(lEJBJNDI);

      String lEJBHomeJNDI =
          "ejb:jeaf-core-integration-test-jee-ear-${project.version}/jeaf-core-service-channel-ejb/LifecycleManagerEJB!com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJBHome";

      lEJBHomeJNDI = XFun.getConfigurationProvider().replaceSystemProperties(lEJBHomeJNDI);
      lLookup = lContext.lookup(lEJBHomeJNDI);
      LifecycleManagerEJBHome lEJBHome = (LifecycleManagerEJBHome) lLookup;
      LifecycleManagerEJB lLifecycleManagerEJB = lEJBHome.create();

      lEJBHome = EJBHelper.lookupEJBHome(lEJBHomeJNDI, LifecycleManagerEJBHome.class);
      lLifecycleManagerEJB = lEJBHome.create();
      lLifecycleManagerEJB.initialize();
      List<String> lAvailableServiceNames = lLifecycleManagerEJB.getAllAvailableServiceNames();
      assertNotNull(lAvailableServiceNames);
      assertEquals(1, lAvailableServiceNames.size());
      assertEquals(GeneratorTestService.class.getName(), lAvailableServiceNames.get(0));

      List<ServiceProxy> lServiceProxies = lLifecycleManagerEJB.getServiceProxies(lAvailableServiceNames);
      assertEquals(1, lServiceProxies.size());

      MessageRepository lRemoteMessageRepository = lLifecycleManagerEJB.getRemoteMessageRepository();
      assertNotNull(lRemoteMessageRepository);

      // Cast service proxy and execute call.
      GeneratorTestService lGeneratorTestService = (GeneratorTestService) lServiceProxies.get(0);
      lGeneratorTestService.doWhatIMean();
    }
  }

  @Test
  @Order(20)
  public void testServiceCalls( ) {
    if (JAVA_VERSION.isEqualOrLower(JavaRelease.JAVA_11)) {
      GeneratorTestService lService = JEAF.getService(GeneratorTestService.class);
      lService.doWhatIMean();
      assertNotNull(lService);
      long lLong = lService.testPrimitiveLongResult();
      assertEquals(4711L, lLong);
      double lDouble = lService.testPrimitveDoubleResult();
      assertEquals(-33.123456789, lDouble);
    }
  }

  @Test
  @Order(30)
  public void testServiceChannelExceptionHandling( ) {
    if (JAVA_VERSION.isEqualOrLower(JavaRelease.JAVA_11)) {
      GeneratorTestService lService = JEAF.getService(GeneratorTestService.class);

      // Expecting nothing to happen
      lService.doWhatIMean(0);

      // Handle NullPointerException on server side.
      try {
        lService.doWhatIMean(1);
        fail("Expecting NullPointerException");
      }
      catch (NullPointerException e) {
        assertEquals("NPE requested by client ;-)", e.getMessage());
      }

      // Handle rollback of current transaction
      try {
        lService.doWhatIMean(2);
        fail("Expecting SystemException");
      }
      catch (SystemException e) {
        assertEquals(MessageConstants.TX_MARKED_FOR_ROLLBACK, e.getErrorCode());
      }
    }
  }
}
