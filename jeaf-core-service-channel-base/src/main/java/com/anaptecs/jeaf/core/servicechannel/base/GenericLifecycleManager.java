/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.anaptecs.jeaf.core.annotations.JEAFActivityImpl;
import com.anaptecs.jeaf.core.api.Activity;
import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceProvider;
import com.anaptecs.jeaf.core.api.jaas.JEAFCallbackHandler;
import com.anaptecs.jeaf.core.servicechannel.api.ActivityRegistry;
import com.anaptecs.jeaf.core.servicechannel.api.ComponentFactory;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.core.servicechannel.api.SchedulerCredentialProvider;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceChannel;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceFactory;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProviderFactory;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProviderRegistry;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceProxy;
import com.anaptecs.jeaf.core.servicechannel.api.ServiceRegistry;
import com.anaptecs.jeaf.core.servicechannel.api.Trigger;
import com.anaptecs.jeaf.core.spi.ComponentImplementation;
import com.anaptecs.jeaf.core.spi.ServiceChannelInterceptor;
import com.anaptecs.jeaf.core.spi.ServiceImplementation;
import com.anaptecs.jeaf.core.spi.ServiceProviderImplementation;
import com.anaptecs.jeaf.tools.api.Tools;
import com.anaptecs.jeaf.tools.api.reflect.ReflectionTools;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.ErrorCode;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This class implements a JEAF life cycle manager. This implementation does not contain any environment specific
 * operations and therefore can be a base class for all life cycle manager implementations. During initialization all
 * components and services will be created / loaded.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public abstract class GenericLifecycleManager extends AbstractLifecycleManager {
  /**
   * Class implements the all steps that have to be performed to start all scheduled triggers. The class implements the
   * interface PrivilegedAction so that the scheduling can be executed using a special security context.
   * 
   * @author JEAF Development Team
   * @version 1.0
   */
  private final class SchedulerAction implements PrivilegedAction<Object> {
    /**
     * Method runs the scheduling by notifying all trigger.
     */
    public Object run( ) {
      // Trace message
      Trace lTrace = XFun.getTrace();
      lTrace.write(MessageConstants.FIREING_TRIGGER, String.valueOf(triggers.size()));
      final long lStartTime = System.currentTimeMillis();

      // Notify all triggers.
      for (Trigger lTrigger : triggers) {
        lTrigger.cycleCompleted();
      }
      // Write trace message again.
      final long lFinishTime = System.currentTimeMillis();
      final long lDuration = (lFinishTime - lStartTime);
      lTrace.write(MessageConstants.ALL_TRIGGERS_COMPLETED, String.valueOf(lDuration));
      return null;
    }
  }

  /**
   * Subject represents the logged in scheduler user. All scheduled tasks are run under the security context of this
   * subject.
   */
  private Subject schedulerSubject;

  /**
   * Map contains all components that have be initialized by this lifecycle manager. The map is never null, since it is
   * created within the class' constructor. Within the map the name of the component is used as key and the Component
   * object as value.
   */
  private Map<String, ComponentImplementation> components;

  /**
   * Map contains the association between a service implementation and the component that provides the service.
   */
  private Map<Class<? extends ServiceImplementation>, ComponentImplementation> serviceComponentMapping;

  /**
   * Reference to the service registry that is used by this lifecycle manager. All created service instances and proxies
   * created by this class will be stored in the referenced service registry. The reference is never null since the
   * object will be created by the class' constructor.
   */
  private final ServiceRegistry serviceRegistry;

  /**
   * Reference to service provider registry that is used by this lifecycle manager. All available activities will be
   * stored here.
   */
  private final ServiceProviderRegistry serviceProviderRegistry;

  /**
   * Reference to activity registry that is used by this lifecycle manager. All available activities will be stored
   * here.
   */
  private final ActivityRegistry activityRegistry;

  /**
   * Reference to the service channel that is used by this lifecycle manager. The reference is never null since the
   * object will be created by the class' constructor.
   */
  private final ServiceChannel serviceChannel;

  /**
   * Set contains all registered triggers.
   */
  private final Set<Trigger> triggers;

  /**
   * Constant for the name of the property file that contains the names of the component factories.
   */
  public static final String COMPONENT_FACTORIES_FILE = "ComponentFactories";

  /**
   * Constant for the property that contains the names of all component factories that should be created.
   */
  public static final String COMPONENT_FACTORY_CLASS_NAMES = "COMPONENT_FACTORY_CLASS_NAMES";

  /**
   * Initialize object.
   */
  public GenericLifecycleManager( ) {
    // Create registries for services, service providers and activities
    serviceRegistry = new ServiceRegistryImpl();
    serviceProviderRegistry = new ServiceProviderRegistryImpl();
    activityRegistry = new ActivityRegistryImpl();

    // Create service channel.
    serviceChannel = this.createServiceChannel();
    Assert.assertNotNull(serviceChannel, "serviceChannel");

    // Create set to store all triggers.
    triggers = new HashSet<Trigger>();
    triggers.addAll(SchedulingConfiguration.getInstance().getTriggers());

    // Create new map for all components.
    components = new HashMap<>();
    serviceComponentMapping = new HashMap<>();
  }

  /**
   * Method creates a new instance of the service channel that should be used in the specific runtime environment for
   * which a concrete life cycle manager implementation is designed for.
   * 
   * @return ServiceChannel Service channel implementation that should be used in this specific environment. The method
   * must not return null.
   */
  protected abstract ServiceChannel createServiceChannel( );

  /**
   * Method returns a UserTransaction object that can be used by the life cycle manager for internal use.
   * 
   * @return {@link UserTransaction} UserTransaction that can be used by the life cycle manager. The method must not
   * return null.
   * @throws NamingException If the JNDI lookup for the UserTransaction object fails.
   */
  protected abstract UserTransaction getUserTransaction( ) throws NamingException;

  /**
   * Method checks if this lifecycle manager implementation supports scheduling. As all implementations based on this
   * lifecycle manager base class support scheduling the method always returns true.
   * 
   * @return boolean Method returns always returns true.
   */
  @Override
  public final boolean supportsScheduling( ) {
    return true;
  }

  /**
   * Method returns the currently defined trigger interval.
   * 
   * @return int Currently defined trigger interval in seconds.
   */
  public final int getTriggerInterval( ) {
    return SchedulingConfiguration.getInstance().getTriggerInterval();
  }

  /**
   * Method notifies all registered triggers that a trigger interval completed.
   */
  public final synchronized void fireTriggers( ) {
    // If a special subject for the scheduler is defined, the scheduling will be executed under a special security
    // context.
    final SchedulerAction lSchedulerAction = new SchedulerAction();
    Subject lSchedulerSubject = this.getSchedulerSubject();
    if (lSchedulerSubject != null) {
      Subject.doAs(lSchedulerSubject, lSchedulerAction);
    }
    // Run scheduling with current security context.
    else {
      lSchedulerAction.run();
    }
  }

  /**
   * Method returns the component to which the passed service implementation belongs to.
   * 
   * @param pService Service implementation to which the providing component should be returned. The parameter must not
   * be null.
   * @return {@link ComponentImplementation} Component that provides the passed service implementation. The method never
   * returns null.
   */
  @Override
  public ComponentImplementation getComponent( ServiceImplementation pService ) {
    return serviceComponentMapping.get(pService.getClass());
  }

  /**
   * Method returns all components that were loaded by this life cycle manager.
   * 
   * @return Collection All loaded components. The method never returns null and the collection can not be edited.
   */
  @Override
  public final Collection<Component> getComponents( ) {
    return Collections.unmodifiableCollection(this.components.values());
  }

  /**
   * Method adds the passed component to the instance of loaded components.
   * 
   * @param pComponent Component that should be added to the list of loaded components. The parameter must not be null.
   */
  private void addComponent( ComponentImplementation pComponent ) {
    // Check parameter.
    Assert.assertNotNull(pComponent, "pComponent");

    // Get component name.
    String lComponentName = pComponent.getName();
    Assert.assertNotNull(lComponentName, "lComponentName");

    // Check if component with the same name was already loaded.
    if (components.containsKey(lComponentName) == false) {
      // Add component to map.
      components.put(lComponentName, pComponent);
    }
    // Component with same name was already loaded.
    else {
      ErrorCode lErrorCode = MessageConstants.COMPONENT_ALREADY_LOADED;
      String[] lParams = new String[] { lComponentName };
      throw new JEAFSystemException(lErrorCode, lParams);
    }
  }

  /**
   * Method returns the service registry that was created during the initialization of JEAF.
   * 
   * @return ServiceRegistry Service registry object that was created during the initialization method
   * <code>initialize()</code>. The method never returns null.
   * @see LifecycleManager#initialize()
   * @see LifecycleManager#getServiceRegistry()
   */
  @Override
  public final ServiceRegistry getServiceRegistry( ) {
    return serviceRegistry;
  }

  /**
   * Method has to return a fully initialized service provider registry for this environment. The method will be called
   * only after initialization ({@link #initialize()}. There is no matter if the service registry is created within this
   * method or before.
   * 
   * @return {@link ServiceProviderRegistry} Service provider registry that will be used in this environment. The method
   * must not return null.
   */
  @Override
  public final ServiceProviderRegistry getServiceProviderRegistry( ) {
    return serviceProviderRegistry;
  }

  /**
   * Method has to return a fully initialized activity registry for this environment. The method will be called only
   * after initialization ({@link #initialize()}. There is no matter if the service registry is created within this
   * method or before.
   * 
   * @return {@link ActivityRegistry} Activity registry that will be used in this environment. The method must not
   * return null.
   */
  @Override
  public final ActivityRegistry getActivityRegistry( ) {
    return activityRegistry;
  }

  /**
   * Method returns the service channel implementation that is used in the context of this lifecycle manager
   * implementation.
   * 
   * @return ServiceChannel Service channel instance for this environment. The method never return null.
   * @see com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager#getServiceChannel()
   */
  @Override
  public final ServiceChannel getServiceChannel( ) {
    return serviceChannel;
  }

  /**
   * Method returns the class objects of all services that were loaded by this life cycle manager.
   * 
   * @return {@link Collection} Class objects of all loaded services. The method never returns null and the collection
   * can not be edited.
   */
  @Override
  public final Collection<Class<? extends Service>> getServices( ) {
    return serviceRegistry.getAvailableServiceClasses();
  }

  /**
   * Method returns the class objects of all service providers that were loaded by this life cycle manager.
   * 
   * @return {@link Collection} Class objects of all loaded service providers. The method never returns null and the
   * collection can not be edited.
   */
  @Override
  public final Collection<Class<? extends ServiceProvider>> getServiceProviders( ) {
    return serviceProviderRegistry.getAvailableServiceProviderClasses();
  }

  /**
   * Method checks the current state of the service implementation that provides the passed service interface.
   * 
   * @param pService Service whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link CheckResult} Object describing the result of the check. The method may return null. This means that
   * the service does not implement any checks.
   */
  @Override
  public final HealthCheckResult checkService( Class<? extends Service> pService, CheckLevel pCheckLevel ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pService, "pService");
    Check.checkInvalidParameterNull(pCheckLevel, "pCheckLevel");

    // Get service and run checks.
    return serviceChannel.checkService(pService, pCheckLevel);
  }

  /**
   * Method checks the current state of the service provider implementation that provides the passed service provider
   * interface.
   * 
   * @param pServiceProviderClass Service Provider whose state should be checked. The parameter must not be null.
   * @param pCheckLevel Level of check that should be performed. The parameter must not be null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service provider does not implement any checks.
   */
  @Override
  public final HealthCheckResult checkServiceProvider( Class<? extends ServiceProvider> pServiceProviderClass,
      CheckLevel pCheckLevel ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pServiceProviderClass, "pServiceProviderClass");
    Check.checkInvalidParameterNull(pCheckLevel, "pCheckLevel");

    // Get service provider and run checks.
    ServiceProviderImplementation lServiceProvider =
        serviceProviderRegistry.getServiceProviderImpl(pServiceProviderClass);
    return lServiceProvider.check(pCheckLevel);
  }

  /**
   * Method implements the startup of the JEAF core. Within the method the following steps are performed in the
   * following order:
   * <ol>
   * <li>Create component factories</li>
   * <li>Create components</li>
   * <li>Create all service instances and proxies using the components service factories</li>
   * <li>Create all activity instances</li>
   * <li>Inject dependencies on all service and activity instances</li>
   * </ol>
   * 
   * @see com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager#performStartup()
   */
  @Override
  public synchronized void performStartup( ) {
    // Write debug about performed steps. This is the first position where debug information could be written. Before
    // this point within the startup the low level parts of the framework are not yet ready in order to generate trace
    // output.

    // Initialize service providers. Since this is more complex than it seams it is extracted to a own method.
    this.initializeServiceProviders();

    // Initialize components.
    this.initializeComponents();

    // Initialize activities.
    this.initializeActivities();

    // Inject dependencies to service instances.
    this.injectDependencies();
  }

  private Subject getSchedulerSubject( ) {
    // Login scheduler user if JEAF Security is enabled.
    if (SecurityConfiguration.getInstance().isJEAFSecurityEnabled() == true) {
      if (schedulerSubject == null) {
        schedulerSubject = this.loginSchedulingUser();
      }
    }
    // The scheduler will be run without a special security context.
    else {
      schedulerSubject = null;
    }
    return schedulerSubject;
  }

  /**
   * Method implements the shutdown of the JEAF core.
   * 
   * @see com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager#performShutdown()
   */
  @Override
  public void performShutdown( ) {
    // Nothing specific to do.
  }

  /**
   * Method initializes all service providers. Since service providers may need a transaction in order to communicate
   * with a resource, this method will start one transaction for all service providers during their initialization.
   */
  private void initializeServiceProviders( ) {
    Trace lTrace = XFun.getTrace();
    lTrace.write(MessageConstants.INITIALIZING_SERVICE_PROVIDERS);

    // The initialization of service providers will be surrounded by a transaction.
    try {
      boolean lRollbackRequired = false;
      boolean lTranscationStarted = false;
      Throwable lThrowable = null;

      // Get user transaction object.
      final UserTransaction lUserTransaction = this.getUserTransaction();

      try {
        // Start transaction.
        lUserTransaction.begin();
        lTranscationStarted = true;

        // Initialize all available service providers.
        this.createServiceProviders();
        // Trace again
        lTrace.write(MessageConstants.INITIALIZING_SERVICE_PROVIDERS_COMPLETED);

        // Commit transaction.
        lUserTransaction.commit();
      }
      // Catch all exception that may occur in the context of the transaction and the initialization of the service
      // providers. In all case the started transaction will be rolled back.
      catch (NotSupportedException | RollbackException | HeuristicMixedException | HeuristicRollbackException
          | SystemException | RuntimeException | Error e) {
        lRollbackRequired = true;
        lThrowable = e;
      }
      // Roll back transaction if required.
      finally {
        // Handle exception.
        if (lThrowable != null) {
          // Try roll back of transaction if required.
          if (lTranscationStarted == true && lRollbackRequired == true) {
            try {
              lUserTransaction.rollback();
            }
            catch (SystemException e) {
              lTrace.write(MessageConstants.UNABLE_TO_ROLLBACK_SERVICE_PROVIDER_TX, e);
            }
          }
          // Throw initially occurred exception wrapped by a JEAFSystemException.
          throw new JEAFSystemException(MessageConstants.EXCEPTION_DURING_SERVICE_PROVIDER_INIT, lThrowable);
        }
      }
    }
    // Lookup for user transaction failed.
    catch (NamingException e) {
      throw new JEAFSystemException(MessageConstants.LOOKUP_FOR_USER_TX_FAILED, e);
    }
  }

  /**
   * Method initializes all configured service provider implementations. Therefore the factory class of all providers
   * has to be configured within the property file <code>jeaf_service_providers.properties</code>.
   * 
   * @throws JEAFSystemException Exception will be thrown if an error occurs during the initialization of a service
   * provider implementation.
   */
  private void createServiceProviders( ) throws JEAFSystemException {
    Trace lTrace = XFun.getTrace();

    // Resolve service provider factories from META-INF directory.
    Set<Class<? extends ServiceProviderFactory>> lServiceProviderFactories =
        new HashSet<>(CoreConfiguration.getInstance().getServiceProviderFactoryClasses());

    // Write trace with debug level.
    final String[] lParams = new String[] { lServiceProviderFactories.toString() };
    lTrace.write(MessageConstants.SERVICE_PROVIDER_FACTORIES_TO_LOAD, lParams);

    // Create all service providers through the defined factories.
    ReflectionTools lReflectionTools = Tools.getReflectionTools();
    for (Class<? extends ServiceProviderFactory> lNextFactoryClass : lServiceProviderFactories) {
      // Create next factory.
      lTrace.write(MessageConstants.CREATING_SERVICE_PROVIDER_FACTORY, lNextFactoryClass.getName());
      ServiceProviderFactory lNextFactory =
          lReflectionTools.newInstance(lNextFactoryClass, ServiceProviderFactory.class);

      // Create new service provider and add it to map with all service providers.
      ServiceProviderImplementation lServiceProviderImpl = lNextFactory.createServiceProviderImplementation();

      // Initialize service provider.
      lTrace.write(MessageConstants.INITIALIZING_SERVICE_PROVIDER, lServiceProviderImpl.getClass().getName());
      lServiceProviderImpl.initialize();

      // Associate service provider with its interface.
      Class<? extends ServiceProvider> lServiceProviderInterface = lNextFactory.getServiceProviderInterface();
      serviceProviderRegistry.registerServiceProvider(lServiceProviderInterface, lServiceProviderImpl);

      // Write trace output.
      String[] lParams2 =
          new String[] { lServiceProviderInterface.getName(), lServiceProviderImpl.getClass().getName() };
      lTrace.write(MessageConstants.SERVICE_PROVIDER_ACCESSABLE, lParams2);
    }
  }

  /**
   * Method initializes all components.
   */
  private void initializeComponents( ) {
    // Create all component factories.
    Collection<ComponentFactory> lComponentFactories = this.createComponentFactories();

    // Create all components
    Iterator<ComponentFactory> lComponentFactoriesIterator = lComponentFactories.iterator();
    while (lComponentFactoriesIterator.hasNext()) {
      // Get next component factory and create new component.
      ComponentFactory lNextComponentFactory = lComponentFactoriesIterator.next();
      ComponentImplementation lComponent = lNextComponentFactory.createComponent();
      this.addComponent(lComponent);

      // Trace information about loaded component.
      this.traceComponentInfo(lComponent);

      // Get all service factories of the current component and use them to create service instances and proxies.
      Collection<ServiceFactory> lServiceFactories = lNextComponentFactory.getServiceFactories();
      for (ServiceFactory lServiceFactory : lServiceFactories) {

        // Get next service factory and register its service.
        ServiceImplementation lServiceInstance = lServiceFactory.createServiceInstance();
        ServiceProxy lServiceProxy = lServiceFactory.createServiceProxy();
        serviceRegistry.registerService(lServiceInstance, lServiceProxy);
        serviceComponentMapping.put(lServiceInstance.getClass(), lComponent);
      }
    }
  }

  /**
   * Method initializes all activities
   */
  private void initializeActivities( ) {
    // Lookup all activities by the defined annotation
    List<Class<? extends Activity>> lActivityImplClasses = CoreConfiguration.getInstance().getActivityImplClasses();
    ReflectionTools lReflectionTools = Tools.getReflectionTools();

    // Create instances for all activities
    for (Class<? extends Activity> lNextClass : lActivityImplClasses) {
      // Create new instance and resolve activity interface
      JEAFActivityImpl lAnnotation = lNextClass.getAnnotation(JEAFActivityImpl.class);
      Class<? extends Activity> lActivityInterface = lAnnotation.activityInterface();
      Activity lNewActivity = (Activity) lReflectionTools.newInstance(lNextClass);
      activityRegistry.registerActivity(lActivityInterface, lNewActivity);
      XFun.getTrace().write(MessageConstants.CREATED_ACTIVITY, lNextClass.getName(), lActivityInterface.getName());
    }
  }

  /**
   * Method creates all component factories that are configured in property file "ComponentFactories.properties".
   * 
   * @return Collection All created component factories. The method never returns null.
   */
  private Collection<ComponentFactory> createComponentFactories( ) {
    // Get name of all component factories.
    List<Class<? extends ComponentFactory>> lFactoryClasses =
        CoreConfiguration.getInstance().getComponentFactoryClasses();

    // Create all component factories.
    ReflectionTools lReflectionTools = Tools.getReflectionTools();
    Collection<ComponentFactory> lComponentFactories = new ArrayList<ComponentFactory>(lFactoryClasses.size());
    for (Class<? extends ComponentFactory> lNextFactory : lFactoryClasses) {
      // Create new factory.
      lComponentFactories.add(lReflectionTools.newInstance(lNextFactory));

      // Trace message
      XFun.getTrace().write(MessageConstants.CREATED_COMPONENT_FACTORY, lNextFactory.getName());
    }

    // Return collection with all created component factories.
    return lComponentFactories;
  }

  /**
   * Method injects all dependencies to all created services and service providers.
   */
  private void injectDependencies( ) {
    // Inject dependencies on all service instances.
    Collection<ServiceImplementation> lServiceInstances = serviceRegistry.getAllServiceInstances();
    DependencyInjector lDependencyInjector = new DependencyInjector(this);
    lDependencyInjector.injectJEAFDependencies(lServiceInstances);

    // Inject dependencies on all service providers
    Collection<ServiceProvider> lServiceProviders = serviceProviderRegistry.getAllServiceProviders();
    lDependencyInjector.injectJEAFDependencies(lServiceProviders);

    // Inject dependencies on all activities
    lDependencyInjector.injectJEAFDependencies(activityRegistry.getAllActivities());
  }

  /**
   * Method tries to create a subject for the scheduler user that is defined in JEAF's properties.
   * 
   * @return {@link Subject} Created subject for the scheduler user. The method never returns null.
   */
  private Subject loginSchedulingUser( ) {
    // Create JEAF specific callback handler and perform JASS login.
    SchedulingConfiguration lConfiguration = SchedulingConfiguration.getInstance();

    Subject lSubject = null;

    if (lConfiguration.isJEAFSchedulingEnabled() == true) {
      SchedulerCredentialProvider lSchedulerCredentialProvider = lConfiguration.getSchedulerCredentialProvider();
      String lUserID = lSchedulerCredentialProvider.getSchedulerUser();
      String lPassword = lSchedulerCredentialProvider.getSchedulerPassword();
      XFun.getTrace().write(MessageConstants.SCHEDULER_USER, lUserID);
      try {
        CallbackHandler lCallbackHandler = new JEAFCallbackHandler(lUserID, lPassword);
        LoginContext loginContext = new LoginContext("JEAFSecurity", lCallbackHandler);
        loginContext.login();
        lSubject = loginContext.getSubject();
        return lSubject;
      }
      catch (LoginException e) {
        throw new JEAFSystemException(MessageConstants.UNABLE_TO_LOGIN_SCHEDULER_USER, e, lUserID);
      }
    }

    // Return created subject or null if scheduling is disabled.
    return lSubject;
  }

  /**
   * Method traces information about the passed component object.
   * 
   * @param pComponent Component about which information should be traced. The parameter must not be null.
   */
  private void traceComponentInfo( ComponentImplementation pComponent ) {
    String lName = pComponent.getName();
    String lClassName = pComponent.getClass().getName();
    String lDescription = pComponent.getDescription();
    String lLayer = pComponent.getLayer().name();
    String lTxBehavior = pComponent.getTransactionBehavior().name();
    String lPersistenceUnit;
    if (pComponent.hasPersistenceUnit()) {
      lPersistenceUnit = pComponent.getPersistenceUnitName();
    }
    else {
      lPersistenceUnit = "none";
    }
    StringBuffer lBuffer = new StringBuffer(pComponent.getServiceChannelInterceptors().size() * 128);
    for (ServiceChannelInterceptor lInterceptor : pComponent.getServiceChannelInterceptors()) {
      lBuffer.append(lInterceptor.getClass().getName());
      lBuffer.append(' ');
    }

    Trace lTrace = pComponent.getTrace();
    lTrace.write(MessageConstants.CREATED_COMPONENT, new String[] { lName, lClassName });
    String[] lParams = new String[] { lName, lDescription, lLayer, lTxBehavior, lPersistenceUnit, lBuffer.toString() };
    lTrace.write(MessageConstants.COMPONENT_DETAILS, lParams);
  }
}