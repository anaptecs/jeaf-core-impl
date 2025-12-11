/**
 * W * Copyright 2004 - 2020 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.ejb.test;

import com.anaptecs.jeaf.accounting.AccountingBasicDataService;
import com.anaptecs.jeaf.accounting.AccountingService;
import com.anaptecs.jeaf.core.annotations.SecurityConfig;
import com.anaptecs.jeaf.junit.core.GeneratorTestService;
import com.anaptecs.jeaf.xfun.annotations.AppInfo;
import com.anaptecs.jeaf.xfun.annotations.RuntimeInfo;
import com.anaptecs.jeaf.xfun.api.info.RuntimeEnvironment;

@AppInfo(applicationID = "JEAFCoreIntegrationTestJEEApp", applicationName = "JEAF Core Integration Test JEE App")

@RuntimeInfo(runtimeEnvironment = RuntimeEnvironment.EJB_CONTAINER)

@SecurityConfig(
    exportedServices = { AccountingService.class, AccountingBasicDataService.class, GeneratorTestService.class })
public interface JEAFCoreIntegrationTestJEE {
}
