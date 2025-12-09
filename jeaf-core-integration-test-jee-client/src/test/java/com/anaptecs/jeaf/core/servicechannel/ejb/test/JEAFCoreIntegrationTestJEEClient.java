/**
 * W * Copyright 2004 - 2020 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.test;

import com.anaptecs.jeaf.core.annotations.CoreConfig;
import com.anaptecs.jeaf.core.annotations.EJBClientServiceChannelConfig;
import com.anaptecs.jeaf.core.annotations.SchedulingConfig;
import com.anaptecs.jeaf.core.annotations.SecurityConfig;
import com.anaptecs.jeaf.core.servicechannel.base.ContextManagerImpl;
import com.anaptecs.jeaf.core.servicechannel.base.DefaultSessionContextManager;
import com.anaptecs.jeaf.core.servicechannel.base.ServiceInvocationContextManagerImpl;
import com.anaptecs.jeaf.core.servicechannel.base.TransactionContextManagerImpl;
import com.anaptecs.jeaf.core.servicechannel.ejb.client.EJBClientLifecycleManager;
import com.anaptecs.jeaf.xfun.annotations.AppInfo;

@AppInfo(applicationID = "JEAFCoreIntegrationTestJEEClient", applicationName = "JEAF Core Integration Test JEE Client")

@CoreConfig(
    lifecycleManager = EJBClientLifecycleManager.class,
    contextManager = ContextManagerImpl.class,
    sessionContextManager = DefaultSessionContextManager.class,
    transactionContextManager = TransactionContextManagerImpl.class,
    serviceInvocationContextManager = ServiceInvocationContextManagerImpl.class)

@SchedulingConfig(enableScheduling = false)

@SecurityConfig(restrictAccessToExportedServices = false, enableSecurity = false)

@EJBClientServiceChannelConfig(
    initialContextFactory = "org.wildfly.naming.client.WildFlyInitialContextFactory",
    providerURL = "http-remoting://localhost:9080",
    lifecycleManagerJNDI = "ejb:jeaf-core-integration-test-jee-ear-${project.version}/jeaf-core-service-channel-ejb/LifecycleManagerEJB!com.anaptecs.jeaf.core.servicechannel.ejb.api.LifecycleManagerEJB",
    serviceChannelJNDI = "ejb:jeaf-core-integration-test-jee-ear-${project.version}/jeaf-core-service-channel-ejb/ServiceChannelEJB!com.anaptecs.jeaf.core.servicechannel.ejb.api.ServiceChannelEJB")

public interface JEAFCoreIntegrationTestJEEClient {
}
