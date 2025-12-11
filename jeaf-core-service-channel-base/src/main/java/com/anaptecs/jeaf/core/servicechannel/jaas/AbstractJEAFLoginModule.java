/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.jaas;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.anaptecs.jeaf.core.api.MessageConstants;
import com.anaptecs.jeaf.core.api.jaas.Group;
import com.anaptecs.jeaf.core.api.jaas.RolePrincipal;
import com.anaptecs.jeaf.core.api.jaas.UserPrincipal;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.trace.Trace;

/**
 * This class implements a JAAS login module. The class therefore uses the data that is stored by JEAF User Management
 * component. This class is the base class for platform specific implementations.
 * 
 * @author JEAF Development Team
 */
public abstract class AbstractJEAFLoginModule implements LoginModule {
  /**
   * Constant for the name of the module option that defines whether the authentication is disabled on login.
   */
  public static final String DISABLE_PASSWORD_CHECK = "disablePasswordCheck";

  /**
   * Constant for the name of the module option that defines whether the authentication is disabled on login.
   */
  public static final String ROLES_GROUP_NAME = "rolesGroupName";

  /**
   * Object will be created during the first phase of the login process and won't be null if the login was successful.
   * If the login method completed successfully the principal is set to a not null value.
   * 
   * @see #doLogin()
   */
  private UserPrincipal principal;

  /**
   * Subject object for the user that is currently logging in. The object is build up within the methods of this class.
   */
  private Subject subject;

  /**
   * The call back handler can be used to request user id and password.
   */
  private CallbackHandler callbackHandler;

  /**
   * Attribute contains the name of the group within subject that is used to store all roles of an user. This value is
   * specific to the used JAAS implementation and thus must be configured for every environment.
   */
  private String rolesGroupName;

  /**
   * Flag indicates whether the commit method was completed successfully.
   */
  private boolean commitCompleted = false;

  /**
   * Initialize object.
   */
  public AbstractJEAFLoginModule( ) {
    // Nothing to do.
  }

  /**
   * Default implementation of JAAS initialize method. The login module only uses the passed subject and the passed call
   * back handler.
   */
  protected final void doInitialize( Subject pSubject, CallbackHandler pCallbackHandler, Map<String, ?> pSharedState,
      Map<String, ?> pOptions ) {
    // Check required parameters for null.
    Assert.assertNotNull(pSubject, "pSubject");
    Assert.assertNotNull(pCallbackHandler, "pCallbackHandler");

    subject = pSubject;
    callbackHandler = pCallbackHandler;

    // Get authentication settings from the passed module options.
    if (pOptions != null) {
      rolesGroupName = (String) pOptions.get(ROLES_GROUP_NAME);
      if (rolesGroupName == null) {
        rolesGroupName = "Roles";
      }
    }
  }

  /**
   * Default implementation of the first part of the login process. The method requests user id and password using the
   * passed callback handlers.
   * 
   * @return boolean The method returns true if the login was successful. The method never returns false since this
   * would mean that the module should be ignored.
   * @throws LoginException As defined by the implemented interface {@link LoginModule} the method throws an exception
   * to indicate a invalid combination of user id and password.
   */
  protected final boolean doLogin( ) throws LoginException {
    // Request user id and password using call back objects.
    Callback[] lCallbacks = new Callback[2];
    NameCallback lNameCallback = new NameCallback("User-ID: ");
    lCallbacks[0] = lNameCallback;

    // For security reason the password should not be displayed.
    final boolean lShowPassword = false;
    PasswordCallback lPasswordCallback = new PasswordCallback("Password: ", lShowPassword);
    lCallbacks[1] = lPasswordCallback;

    // Let call back handler request the required information
    try {
      callbackHandler.handle(lCallbacks);

      // Get user id and password.
      String lUserID = lNameCallback.getName();

      // If the login will be performed for "anonymous" the returned user id is null and a default principal user id
      // will be used for the login of an anonymous user
      if (lUserID == null) {
        lUserID = UserPrincipal.ANONYMOUSID;
      }

      // Get password.
      final char[] lPasswordChar = lPasswordCallback.getPassword();
      String lPassword = "";
      if (lPasswordChar != null) {
        lPassword = new String(lPasswordChar);
      }

      // Clear password from call back object.
      lPasswordCallback.clearPassword();

      // Try login via JEAF AuthorizationServiceProvider
      Trace lTrace = XFun.getTrace();
      String lModuleImplName = this.getClass().getSimpleName();
      lTrace.write(MessageConstants.TRYING_LOGIN, lUserID, lModuleImplName);
      principal = this.performLogin(lUserID, lPassword);
      lTrace.write(MessageConstants.LOGIN_SUCCESSFUL, lUserID, lModuleImplName);

      // Always return true. If the login fails an exception will be thrown.
      return true;
    }
    // Rethrow exception as LoginException
    catch (IOException e) {
      throw new LoginException(e.getMessage());
    }
    // Rethrow exception as LoginException
    catch (UnsupportedCallbackException e) {
      throw new LoginException(e.getMessage());
    }
  }

  /**
   * This is the second phase of the login process. During this phase the created principal is added to the subject of
   * the logged in user.
   * 
   * @return boolean The method returns true if the commit was successful and false if this login module can be ignored.
   */
  protected final boolean doCommit( ) throws LoginException {
    // Was execution of login method successful?
    if (principal != null) {
      // Add created principal to the passed subject.
      subject.getPrincipals().add(principal);

      // Create roles and add them to subject.
      Group lRolesGroup = new Group(rolesGroupName);
      for (String lRoleName : principal.getAssignedRoles()) {
        lRolesGroup.addMember(new RolePrincipal(lRoleName));
      }
      subject.getPrincipals().add(lRolesGroup);

      commitCompleted = true;
      XFun.getTrace().write(MessageConstants.COMMIT_SUCCESSFUL, principal.getLoginName());
    }
    // Login was not successful before thus this login module can be ignored.
    else {
      commitCompleted = false;
    }
    return commitCompleted;
  }

  /**
   * This is the second phase of the login process. This method will be called if at least one login module could not
   * successfully complete the first phase.
   * 
   * @return boolean The method returns true if the commit was successful and false if this login module can be ignored.
   */
  protected final boolean doAbort( ) throws LoginException {
    boolean lResult;
    // If the principal is null this means that the login method completed and thus this login module can be ignored.
    if (principal == null) {
      lResult = false;
    }
    // Login process was not completed successfully.
    else if (commitCompleted == false) {
      // Cleanup up.
      this.resetAttributes();
      lResult = true;
    }
    // Login completed successful thus a complete logout has to be performed.
    else {
      this.logout();
      lResult = true;
    }
    return lResult;
  }

  /**
   * Method performs a logout of the user that is represented by the subject and its principal.
   * 
   * @return boolean Method always returns true since this method must only be called if the login process completed
   * successfully.
   */
  protected final boolean doLogout( ) {
    // Ensure that the required attributes are set.
    Assert.assertNotNull(principal, "principal");
    Assert.assertNotNull(subject, "subject");

    // Remove added principal and reset all attributes afterwards.
    subject.getPrincipals().remove(principal);
    this.resetAttributes();

    // Always return true.
    return true;
  }

  /**
   * Method resets all attributes of this object.
   */
  private void resetAttributes( ) {
    // Set all attributes to null / initial value.
    principal = null;
    subject = null;
    callbackHandler = null;
    commitCompleted = false;
  }

  /**
   * Method returns the principal of the logged in user.
   * 
   * @return {@link UserPrincipal} Principal of the logged in user. The method returns null if no user is logged in.
   */
  protected final UserPrincipal getPrincipal( ) {
    return principal;
  }

  /**
   * Method performs the actual login. Therefore it checks whether the passed combination of user id and password is
   * valid and if the user account is still valid. In this case valid means that the account is within its validity
   * period, the password is not expired and the account is not locked.
   * 
   * @param pUserID User id of the account. The parameter must not be null.
   * @param pPassword Not encrypted password of the user. The parameter must not be null.
   * @return {@link UserPrincipal} Created principal object for the passed user account. The method never returns null.
   * @throws LoginException If an error occurs during the login. All thrown exceptions match the requirements defined by
   * the JASS specification and thus should only be called.
   */
  protected abstract UserPrincipal performLogin( String pUserID, String pPassword ) throws LoginException;
}