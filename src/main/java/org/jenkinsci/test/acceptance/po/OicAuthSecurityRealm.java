package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;

/**
 * Security Realm provided by oic-auth plugin
 */
@Describable("Login with Openid Connect")
public class OicAuthSecurityRealm extends SecurityRealm {

    public OicAuthSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    public void configureClient(String clientId, String clientSecret) {
        control("clientId").set(clientId);
        control("clientSecret").set(clientSecret);
    }

    public <T extends OicAuthConfigurationMode> T useConfigurationMode(Class<T> type) {
        WebElement option = findCaption(type, caption -> getElement(by.option(caption)));
        option.click();
        return newInstance(type, this);
    }

    public void setLogoutFromOpenidProvider(boolean logout) {
        Control check = control(by.checkbox("Logout from OpenID Provider"));
        if (logout) {
            check.check();
        } else {
            check.uncheck();
        }
    }

    public void setPostLogoutUrl(String postLogoutUrl) {
        control("postLogoutRedirectUrl").set(postLogoutUrl);
    }

    public void setUserFields(
            String userNameFieldName, String emailFieldName, String fullNameFieldName, String groupsFieldName) {
        clickButton("User fields");
        waitFor(by.path("/securityRealm/groupsFieldName"));
        control("userNameField").set(userNameFieldName);
        control("emailFieldName").set(emailFieldName);
        control("fullNameFieldName").set(fullNameFieldName);
        control("groupsFieldName").set(groupsFieldName);
    }
}
