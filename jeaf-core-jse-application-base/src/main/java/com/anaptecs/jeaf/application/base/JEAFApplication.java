/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.application.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.jaas.JAASConstants;
import com.anaptecs.jeaf.core.api.jaas.JEAFCallbackHandler;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.config.Configuration;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * Class is the base class for JEAF based command line applications and therefore provides a simple mechanism to ease
 * the handling of parameters that are passed to a command line application.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public abstract class JEAFApplication {
  /**
   * Constant for the prefix of all options.
   */
  public static final String OPTION_PREFIX = "-";

  /**
   * Login context represents the logged in user.
   */
  private static LoginContext loginContext;

  /**
   * "Input file / directory" "output directory" [file exclusion list] -option1 -option2
   * 
   * @param pInitializeJEAF Parameter indicates whether JEAF should be initialized or not.
   * @throws LoginException
   */
  public JEAFApplication( boolean pInitializeJEAF ) throws LoginException {
    this(pInitializeJEAF, true);
  }

  /**
   * "Input file / directory" "output directory" [file exclusion list] -option1 -option2
   * 
   * @param pInitializeJEAF Parameter indicates whether JEAF should be initialized or not.
   * @param pTryLogin Parameter defines if a login should be performed.
   * @throws LoginException
   */
  public JEAFApplication( boolean pInitializeJEAF, boolean pTryLogin ) throws LoginException {
    // Show application info.
    Trace lTrace = XFun.getTrace();
    List<String> lApplicationInfo = this.getApplicationInfo();
    if (lApplicationInfo != null) {
      for (String lString : lApplicationInfo) {
        lTrace.info(lString);
      }
    }
    lTrace.info("Working directory: " + System.getProperty("user.dir"));
    lTrace.info("Classpath: " + System.getProperty("java.class.path"));
    lTrace.info("");

    // The very first thing that has to be done is to initialize JEAF.
    if (pInitializeJEAF == true) {
      this.initialize();
    }

    // Try to login.
    if (pTryLogin == true) {
      this.login();
    }
    else {
      XFun.getTrace().info("Starting application without login.");
    }
  }

  /**
   * Initialize ...
   */
  protected void initialize( ) {
    // As soon as entry point JEAF is loaded we are fine.
    JEAF.load();
    JEAF.injectDependencies(this);
  }

  /**
   * Method tries to perform a JAAS login
   * 
   * @throws LoginException
   */
  public void login( ) throws LoginException {
    // As soon as a login module is configured, JEAF will try to login the defined user.
    if (this.isLoginModuleDefined()) {
      Configuration lConfiguration = XFun.getConfigurationProvider().getSystemPropertiesConfiguration();
      String lLoginName = lConfiguration.getConfigurationValue("jeaf.user", null, String.class);
      String lPassword = lConfiguration.getConfigurationValue("jeaf.password", "", String.class);

      // Create JEAF specific callback handler and perform JASS login.
      CallbackHandler lCallbackHandler = new JEAFCallbackHandler(lLoginName, lPassword);
      loginContext = new LoginContext("JEAFSecurity", lCallbackHandler);
      loginContext.login();
    }

    // Inject dependencies to all referenced services. As long as no login was performed we can not inject dependencies.
    JEAF.injectDependencies(this);
  }

  public void logout( ) throws LoginException {
    loginContext.logout();
  }

  /**
   * Method returns the subject of the current user.
   * 
   * @return {@link Subject} Subject representing the current user. If no user is logged in then the method returns
   * null.
   */
  static Subject getCurrentUser( ) {
    Subject lSubject;
    if (loginContext != null) {
      lSubject = loginContext.getSubject();
    }
    else {
      lSubject = null;
    }
    return lSubject;
  }

  /**
   * Method checks whether a JASS Login Module is defined or not.
   * 
   * @return boolean The method returns true if a JAAS login module is defined and false in all other cases.
   */
  private boolean isLoginModuleDefined( ) {
    return System.getProperty(JAASConstants.LOGIN_MODULE_PROPERTY) != null;
  }

  /**
   * Method starts the execution of the command line application.
   * 
   * @param pArguments Arguments that were passed to the application. The parameter must not be null.
   * @return int The method returns 0 if the execution was successful and -1 in all other cases.
   */
  public final int start( String[] pArguments ) {
    // Check parameter.
    Check.checkInvalidParameterNull(pArguments, "pArguments");

    int lExitCode;
    try {
      // Get input files, output directory and options
      final List<String> lInputFileNames = this.getInputFileNames(pArguments);
      final String lOutputDirectoryName = this.getOutputDirectoryName(pArguments);
      final Map<String, String> lOptions = this.getOptions(pArguments);

      // Run application
      this.runApplication(lInputFileNames, lOutputDirectoryName, lOptions);
      lExitCode = 0;
    }
    catch (Exception e) {
      XFun.getTrace().error(e.getMessage(), e);
      lExitCode = -1;
    }
    return lExitCode;
  }

  /**
   * Method contains the concrete implementation of the command line application. This method has to be implemented by
   * subclasses.
   * 
   * @param pInputFileNames List contains the names of all files that should be handled by the application. The
   * parameter is never null.
   * @param pOutputDirectoryName Absolute path of the output directory where results should be written to. The parameter
   * may be null.
   * @param pOptions Options that are passed to the application. The options are represented as key value pairs. Options
   * have to start with "-" as prefix and "=" as delimiter to separate keys and values. If an option does not have a
   * value the passed map will contain null. All keys will be to lower case. If no options are passed then the map will
   * be empty. In order to provide default values for some options you can override operation
   * {@link #getDefaultOptions()}
   * 
   * @see #getDefaultOptions()
   */
  protected abstract void runApplication( List<String> pInputFileNames, String pOutputDirectoryName,
      Map<String, String> pOptions )
    throws Exception;

  /**
   * Method returns if the concrete command line application has files as input or not. This influences how parameters
   * that are passed to the application will be handled.
   * 
   * @return boolean Method returns true if the application expects files as input and false in all other cases.
   */
  protected abstract boolean hasFileInput( );

  /**
   * Method returns if the concrete command line application writes files as output or not. This influences how
   * parameters that are passed to the application will be handled.
   * 
   * @return boolean Method returns true if the application expects files as input and false in all other cases.
   */
  protected abstract boolean hasFileOutput( );

  /**
   * Method prints the usage of the application. This method is intended to be implemented by subclasses in order to
   * describe their set of supported options.
   */
  protected abstract void printUsage( );

  /**
   * Method returns a description of the application that is show on its startup. Subclasess may override this method.
   * 
   * @return {@link List} List of string. Each of the string will be used as one line. The method may return null.
   */
  protected List<String> getApplicationInfo( ) {
    return null;
  }

  /**
   * Method returns a map contains containing the default values for options. These options and their values will be
   * used if they are not overridden by parameters passed to the application. In order to define options and the default
   * values subclasses can override this method.
   * 
   * @return {@link Map} Map with the default options and their values. The method must not return null. This default
   * implementation returns an empty map.
   */
  protected Map<String, String> getDefaultOptions( ) {
    return new HashMap<String, String>();
  }

  /**
   * Method returns the extensions of all supported file types.
   * 
   * @return {@link List} List with the extensions of all supported file types. The method must not return null.
   */
  protected abstract List<String> getSupportedExtensions( );

  /**
   * Method returns the name of all input files that are passed to the command line application. The input files are
   * determined by the first argument and exclusion list.
   * 
   * @param pArguments Arguments that were passed to the application. The parameter must not be null.
   * @return {@link List} Names of all input files. If no input file or directory is defined the method returns an empty
   * list.
   */
  private List<String> getInputFileNames( String[] pArguments ) {
    // Check parameter.
    Assert.assertNotNull(pArguments, "pArguments");

    // Input file / directory is defined.
    List<String> lInputFileNames;
    if (this.hasFileInput() == true && pArguments.length > 0) {
      // Check whether first parameter points to a single file or a directory.
      File lResourceLocation = new File(pArguments[0]);

      // Get all files from the directory.
      if (lResourceLocation.isDirectory() == true) {
        // Calculate offset for file exclusions.
        int lOffset = 0;
        if (this.hasFileOutput() == true) {
          lOffset++;
        }
        // Create exclusion list.
        List<String> lExclusionList = new ArrayList<String>();
        for (int i = lOffset; i < pArguments.length; i++) {
          String lNextArgument = pArguments[i];
          if (lNextArgument.startsWith(OPTION_PREFIX) == false) {
            lExclusionList.add(lNextArgument);
          }
        }

        // Create FileFilter and determine resource files that should be used.
        List<String> lExtensions = this.getSupportedExtensions();
        FileFilter lFileFilter = new FileFilter(lExtensions, lExclusionList);
        File[] lFiles = lResourceLocation.listFiles(lFileFilter);

        // Ensure that files exist
        if (lFiles != null) {
          lInputFileNames = new ArrayList<String>(lFiles.length);

          // Add absolute location of files to Collection with resource files.
          for (int i = 0; i < lFiles.length; i++) {
            lInputFileNames.add(lFiles[i].getAbsolutePath());
          }
        }
        else {
          lInputFileNames = Collections.emptyList();
        }
      }
      // Only single resource file was provided.
      else {
        lInputFileNames = new ArrayList<String>(1);
        lInputFileNames.add(pArguments[0]);
      }
    }
    // No input file or directory defined.
    else {
      lInputFileNames = Collections.emptyList();
    }
    return lInputFileNames;
  }

  /**
   * Method returns the absolute path of the output directory that is defined by the second argument.
   * 
   * @param pArguments Arguments that were passed to the application. The parameter must not be null.
   * @return {@link String} Absolute path of the output directory. The method returns null if no output directory is
   * defined.
   */
  private String getOutputDirectoryName( String[] pArguments ) throws IOException {
    // Check parameter.
    Assert.assertNotNull(pArguments, "pArguments");

    // Calculate position in array were the output could be found.
    int lOffset;
    if (this.hasFileInput() == true) {
      lOffset = 1;
    }
    else {
      lOffset = 0;
    }

    final String lOutputDirectoryName;
    if (this.hasFileOutput() == true && pArguments.length > lOffset) {
      // Determine output directory and check if it is valid.
      File lOutputDirectory = new File(pArguments[lOffset]);
      if (lOutputDirectory.isDirectory() == false) {
        lOutputDirectoryName = lOutputDirectory.getAbsolutePath();
        return lOutputDirectoryName;
      }
      // Parameter pArguments[1] is no a valid output directory.
      else {
        String lMessage = "'" + lOutputDirectory.getName() + "' is not a valid output directory.";
        throw new IOException(lMessage);
      }
    }
    else {
      lOutputDirectoryName = null;
    }
    return lOutputDirectoryName;
  }

  /**
   * Method returns all options that were passed to the application. An option is an argument that starts with "-".
   * 
   * @param pArguments Arguments that were passed to the application. The parameter must not be null.
   * @return {@link List} List contains all options that were passed. The method returns an empty list if no options
   * were passed.
   */
  private Map<String, String> getOptions( String[] pArguments ) {
    // Check parameter.
    Assert.assertNotNull(pArguments, "pArguments");

    // Calculate position in array were the output could be found.
    int lOffset = 0;
    if (this.hasFileInput() == true) {
      lOffset++;
    }
    if (this.hasFileOutput() == true) {
      lOffset++;
    }

    // Start looking for options from the third argument on
    Map<String, String> lOptions = this.getDefaultOptions();
    for (int i = lOffset; i < pArguments.length; i++) {
      String lNextArgument = pArguments[i];
      if (lNextArgument.startsWith(OPTION_PREFIX) == true) {
        String lOptionSubstring = lNextArgument.substring(OPTION_PREFIX.length());

        String[] lSplit = lOptionSubstring.split("=");
        String lKey = lSplit[0].toLowerCase();
        String lValue;
        if (lSplit.length > 1) {
          lValue = lSplit[1];
        }
        else {
          lValue = null;
        }
        lOptions.put(lKey, lValue);
      }
    }
    return lOptions;
  }
}