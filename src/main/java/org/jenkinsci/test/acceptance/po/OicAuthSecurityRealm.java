package org.jenkinsci.test.acceptance.po;

/**
 * Security Realm provided by oic-auth plugin
 */
@Describable({"Login with Openid Connect", "Login with Openid Connect"})
public class OicAuthSecurityRealm extends SecurityRealm {

    public OicAuthSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    public void configureClient(String clientId, String clientSecret) {
        control("clientId").set(clientId);
        control("clientSecret").set(clientSecret);
    }

    public void setAutomaticConfiguration(String wellKnownEndpoint) {
        control(by.radioButton("Automatic configuration")).click();
        control("wellKnownOpenIDConfigurationUrl").set(wellKnownEndpoint);
    }

    public void logoutFromOpenidProvider(boolean logout) {
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
}
