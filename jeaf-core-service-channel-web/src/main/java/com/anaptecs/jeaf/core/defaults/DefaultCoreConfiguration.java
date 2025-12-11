/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.defaults;

import com.anaptecs.jeaf.core.annotations.CoreConfig;
import com.anaptecs.jeaf.core.annotations.SchedulingConfig;
import com.anaptecs.jeaf.core.annotations.SecurityConfig;
import com.anaptecs.jeaf.core.jee.servlet.WebSessionContextManager;
import com.anaptecs.jeaf.core.servicechannel.base.ContextManagerImpl;
import com.anaptecs.jeaf.core.servicechannel.base.DefaultSchedulerCredentialProvider;
import com.anaptecs.jeaf.core.servicechannel.base.DefaultSecurityServiceChannelInterceptor;
import com.anaptecs.jeaf.core.servicechannel.base.ServiceInvocationContextManagerImpl;
import com.anaptecs.jeaf.core.servicechannel.base.TransactionContextManagerImpl;
import com.anaptecs.jeaf.core.servicechannel.unmanaged.LocalLifecycleManagerImpl;

@CoreConfig(
    lifecycleManager = LocalLifecycleManagerImpl.class,
    contextManager = ContextManagerImpl.class,
    sessionContextManager = WebSessionContextManager.class,
    transactionContextManager = TransactionContextManagerImpl.class,
    serviceInvocationContextManager = ServiceInvocationContextManagerImpl.class)

@SchedulingConfig(
    enableScheduling = SchedulingConfig.JEAF_SCHEDULING_DEFAULT,
    triggerInterval = SchedulingConfig.TRIGGER_INTERVAL_DEFAULT,
    schedulerCredentialProvider = DefaultSchedulerCredentialProvider.class)

@SecurityConfig(
    restrictAccessToExportedServices = SecurityConfig.LIMIT_ACCESS_TO_PUBLIC_SERVICES_DEFAULT,
    enableSecurity = SecurityConfig.JEAF_SECURITY_DEFAULT,
    securityInterceptor = DefaultSecurityServiceChannelInterceptor.class)

/**
 * No matter what kind of implementation of JEAF Core is used a default configuration always has to be available with
 * this package.
 */
public interface DefaultCoreConfiguration {
}
