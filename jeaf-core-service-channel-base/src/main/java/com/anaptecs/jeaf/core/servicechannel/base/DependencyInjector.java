/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import com.anaptecs.jeaf.core.annotations.JEAFActivity;
import com.anaptecs.jeaf.core.annotations.JEAFService;
import com.anaptecs.jeaf.core.annotations.JEAFServiceProvider;
import com.anaptecs.jeaf.core.annotations.JEAFTrace;
import com.anaptecs.jeaf.core.api.Activity;
import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.Service;
import com.anaptecs.jeaf.core.api.ServiceProvider;
import com.anaptecs.jeaf.core.servicechannel.api.LifecycleManager;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.errorhandling.JEAFSystemException;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * Class is responsible to inject dependencies to objects.
 * 
 * @author JEAF Development Team
 */
class DependencyInjector {
  /**
   * Reference to lifecycle manager. Lifecycle manager is needed to resolve the declared dependencies.
   */
  private final LifecycleManager lifecycleManager;

  /**
   * Initialize dependency injector.
   * 
   * @param pLifecycleManager Reference to lifecycle manager that should be used to resolve dependencies. The parameter
   * must not be null.
   */
  DependencyInjector( LifecycleManager pLifecycleManager ) {
    // Check parameter
    Assert.assertNotNull(pLifecycleManager, "pLifecycleManager");

    lifecycleManager = pLifecycleManager;
  }

  /**
   * Method injects all dependencies to JEAF services to the passed object. Therefore the method checks all fields of
   * the class and all of its parents classes for annotated fields that declare a dependency to a JEAF service.
   * 
   * @param pObject Object on which the dependencies should be injected. The parameter must not be null and the passed
   * object will be modified.
   */
  void injectJEAFDependencies( Object pObject ) {
    // Check parameter.
    Check.checkInvalidParameterNull(pObject, "pObject");

    // Inject dependencies to all services
    this.injectServiceDependencies(pObject);

    // Inject dependencies to all service providers
    this.injectServiceProviderDependencies(pObject);

    // Inject dependencies to all service providers
    this.injectActivityDependencies(pObject);

    // Inject dependency to trace object.
    this.injectTraceDependencies(pObject);
  }

  /**
   * Method injects all dependencies to JEAF services to the passed objects. Therefore the method checks all fields of
   * the classes and all of its parents classes for annotated fields that declare a dependency to a JEAF service.
   * 
   * @param pObject Object on which the dependencies should be injected. The parameter must not be null and the passed
   * object will be modified.
   */
  public void injectJEAFDependencies( Collection<?> pObjects ) {
    // Check parameter.
    Check.checkInvalidParameterNull(pObjects, "pObjects");

    // Inject dependencies to all passed objects.
    for (Object lNextObject : pObjects) {
      this.injectJEAFDependencies(lNextObject);
    }
  }

  /**
   * Method injects dependencies to all service that are declared by the passed objects.
   * 
   * @param pObject Object on which all dependencies should be injected. The parameter must not be null.
   */
  private void injectServiceDependencies( Object pObject ) {
    // Get all fields that declare a dependency to a JEAF service.
    Class<?> lClass = pObject.getClass();
    Collection<Field> lAnnotatedFields = this.getAnnotatedFields(lClass, JEAFService.class);

    // Inject dependencies on all fields
    for (Field lNextField : lAnnotatedFields) {
      // Lookup desired service.
      @SuppressWarnings("unchecked")
      Service lDependency =
          lifecycleManager.getServiceRegistry().getServiceProxy((Class<? extends Service>) lNextField.getType());

      // Inject service on object.
      this.injectObject(pObject, lNextField, lDependency);
    }
  }

  /**
   * Method injects dependencies to all service providers that are declared by the passed objects.
   * 
   * @param pObject Object on which all dependencies should be injected. The parameter must not be null.
   */
  private void injectServiceProviderDependencies( Object pObject ) {
    // Get all fields that declare a dependency to a JEAF service.
    Class<?> lClass = pObject.getClass();
    Collection<Field> lAnnotatedFields = this.getAnnotatedFields(lClass, JEAFServiceProvider.class);

    // Inject dependencies on all fields
    for (Field lNextField : lAnnotatedFields) {
      // Lookup desired service provider.
      @SuppressWarnings("unchecked")
      ServiceProvider lDependency = lifecycleManager.getServiceProviderRegistry()
          .getServiceProvider((Class<? extends ServiceProvider>) lNextField.getType());

      // Inject service on object.
      this.injectObject(pObject, lNextField, lDependency);
    }
  }

  /**
   * Method injects dependencies to all activities that are declared by the passed objects.
   * 
   * @param pObject Object on which all dependencies should be injected. The parameter must not be null.
   */
  private void injectActivityDependencies( Object pObject ) {
    // Get all fields that declare a dependency to a JEAF service.
    Class<?> lClass = pObject.getClass();
    Collection<Field> lAnnotatedFields = this.getAnnotatedFields(lClass, JEAFActivity.class);

    // Inject dependencies on all fields
    for (Field lNextField : lAnnotatedFields) {
      // Lookup desired activity.
      @SuppressWarnings("unchecked")
      Activity lDependency =
          lifecycleManager.getActivityRegistry().getActivity((Class<? extends Activity>) lNextField.getType());

      // Inject service on object.
      this.injectObject(pObject, lNextField, lDependency);
    }
  }

  /**
   * Method injects dependencies to the current trace object for the passed object.
   * 
   * @param pObject Object on which the trace dependency should be injected. The parameter must not be null.
   */
  private void injectTraceDependencies( Object pObject ) {
    // Get all fields that declare a dependency to a JEAF service.
    Class<? extends Object> lClass = pObject.getClass();
    Collection<Field> lAnnotatedFields = this.getAnnotatedFields(lClass, JEAFTrace.class);

    // Inject dependencies on all fields
    for (Field lNextField : lAnnotatedFields) {
      // Get current trace
      Trace lDependency = XFun.getTrace();

      // Inject service on object.
      this.injectObject(pObject, lNextField, lDependency);
    }
  }

  /**
   * Method returns all fields of the class and all of it parent classes, that have a dependency to any JEAFService
   * (defined by an annotation).
   * 
   * @param pClass Class for which all dependencies to JEAF services should be returned. *
   * @return {@link Collection} Collection contains all fields of the class and its parent classes that declare a
   * dependency to a JEAF service. The method never returns null.
   */
  private Collection<Field> getAnnotatedFields( Class<?> pClass, Class<? extends Annotation> pAnnotation ) {
    // Check parameter.
    Assert.assertNotNull(pClass, "pClass");
    Assert.assertNotNull(pAnnotation, "pAnnotation");

    Collection<Field> lAnnotatedFields = new ArrayList<Field>(0);
    for (Field lNextField : pClass.getDeclaredFields()) {
      if (lNextField.isAnnotationPresent(pAnnotation) == true) {
        lAnnotatedFields.add(lNextField);
      }
    }

    // Check parent class.
    Class<?> lSuperclass = pClass.getSuperclass();
    if (lSuperclass != null) {
      Collection<Field> lSuperclassDependencies = this.getAnnotatedFields(lSuperclass, pAnnotation);
      lAnnotatedFields.addAll(lSuperclassDependencies);
    }

    // Return all annotated fields.
    return lAnnotatedFields;
  }

  /**
   * Method injects the passed dependency on the passed object.
   * 
   * @param pObject Object on which the dependent object should be injected. The parameter must not be null.
   * @param pField Fields of the object on which the dependent object should be injected. The parameter must not be
   * null.
   * @param pDependency Dependent object that should be injected on the object. The parameter must not be null.
   */
  private void injectObject( Object pObject, Field pField, Object pDependency ) {
    // Check parameters.
    Check.checkInvalidParameterNull(pObject, "pObject");
    Check.checkInvalidParameterNull(pField, "pField");
    Check.checkInvalidParameterNull(pDependency, "pDependency");

    try {
      // Write trace message
      XFun.getTrace().write(MessageConstants.INJECTING_DEPENDENCY, pField.getType().getName(),
          pObject.getClass().getName());

      // Inject dependency via reflection call.
      pField.setAccessible(true);
      pField.set(pObject, pDependency);
    }
    // Handle exception of reflection cause.
    catch (IllegalAccessException e) {
      throw new JEAFSystemException(MessageConstants.UNABLE_TO_INJECT_DEPENDENCY, e, pField.getType().getName(),
          pObject.getClass().getName());
    }
  }
}
