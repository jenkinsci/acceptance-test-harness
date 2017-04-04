package org.jenkinsci.test.acceptance.po;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapEnvironmentVariable;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapGroupMembershipStrategy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * SecurityRealm for ldap plugin.
 *
 * @author Michael Prankl
 * @see org.jenkinsci.test.acceptance.po.LdapSecurityRealm_Pre1_10 if you want to test versions of the plugin < 1.10
 */
@Describable("LDAP")
public class LdapSecurityRealm<T extends LdapGroupMembershipStrategy> extends SecurityRealm {

    private GlobalSecurityConfig context;

    protected final Control ldapServer = control("server");
    protected final Control advanced = control("advanced-button");
    protected final Control rootDn = control("rootDN");
    protected final Control managerDn = control("managerDN");
    protected final Control managerPassword = control("managerPasswordSecret"/* >= 1.9*/, "managerPassword");
    protected final Control userSearchBase = control("userSearchBase");
    protected final Control userSearchFilter = control("userSearch");
    protected final Control groupSearchBase = control("groupSearchBase");
    protected final Control groupSearchFilter = control("groupSearchFilter");
    /**
     * only available prior ldap plugin version 1.10
     */
    protected final Control groupMembershipFilter = control("groupMembershipFilter");
    protected final Control disableLdapEmailResolver = control("disableMailAddressResolver");
    protected final Control enableCache = control("cache");
    protected final Control addEnvVariableButton = control("repeatable-add");
    /**
     * since version 1.8
     */
    private final Control displayNameAttributeName = control("displayNameAttributeName");
    /**
     * since version 1.8
     */
    private final Control mailAddressAttributeName = control("mailAddressAttributeName");
    /**
     * since version 1.15
     */
    private final Control validateButton = control("validateLdapSettings");

    public LdapSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
        this.context = context;
    }

    private T useGroupMembershipStrategy(Class<T> type) {
        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override
            protected WebElement find(String caption) {
                return getElement(by.radioButton(caption));
            }
        });
        radio.click();
        return newInstance(type, this.context, radio.getAttribute("path"));
    }

    /**
     * Fills the input fields for ldap access control.
     */
    public void configure(LdapDetails<T> ldapDetails) {
        ldapServer.set(ldapDetails.getHostWithPort());
        advanced.click();
        rootDn.set(ldapDetails.getRootDn());
        managerDn.set(ldapDetails.getManagerDn());
        managerPassword.set(ldapDetails.getManagerPassword());
        userSearchBase.set(ldapDetails.getUserSearchBase());
        userSearchFilter.set(ldapDetails.getUserSearchFilter());
        groupSearchBase.set(ldapDetails.getGroupSearchBase());
        groupSearchFilter.set(ldapDetails.getGroupSearchFilter());
        configureGroupMembership(ldapDetails);
        disableLdapEmailResolver.check(ldapDetails.isDisableLdapEmailResolver());
        if (ldapDetails.isEnableCache()) {
            enableCache.check(true);
            control("cache/size").select(String.valueOf(ldapDetails.getCacheSize()));
            control("cache/ttl").select(String.valueOf(ldapDetails.getCacheTTL()));
        }
        if (ldapDetails.getDisplayNameAttributeName() != null) {
            displayNameAttributeName.set(ldapDetails.getDisplayNameAttributeName());
        }
        if (ldapDetails.getMailAddressAttributeName() != null) {
            mailAddressAttributeName.set(ldapDetails.getMailAddressAttributeName());
        }
        if (ldapDetails.getEnvironmentVariables() != null && !ldapDetails.getEnvironmentVariables().isEmpty()) {
            int i = 0;
            String envVarSelector;
            for (LdapEnvironmentVariable envVariable : ldapDetails.getEnvironmentVariables()) {
                addEnvVariableButton.click();
                envVarSelector = i == 0 ? "" : "[" + i + "]";
                control("/environmentProperties" + envVarSelector + "/name").set(envVariable.getName());
                control("/environmentProperties" + envVarSelector + "/value").set(envVariable.getValue());
                i++;
            }
        }
    }

    public void validate(String username, String password) {
        final String id = validateButton.resolve().getAttribute("id");
        final By by = By.id(id+"_dialog");
        for (WebElement element : all(by)) {
            if (element.isDisplayed()) {
                throw new AssertionError(String.format("Form at path %s is already visible", element.getAttribute("path")));
            }
        }
        validateButton.click();
        String formPath = waitFor().withTimeout(10, TimeUnit.SECONDS).until(new Function<CapybaraPortingLayer, String>() {
            @Nullable
            @Override
            public String apply(@Nullable CapybaraPortingLayer input) {
                for (WebElement element: all(by)) {
                    if (element.isDisplayed()) {
                        return id+"_dialog";
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "Form area to appear: " + by;
            }
        });
        new Validation(this.getPage(), formPath).submit(username, password);
        ldapServer.click();
    }

    /**
     * Subclasses can override this to handle group membership differently.
     */
    protected void configureGroupMembership(LdapDetails<T> ldapDetails) {
        if (ldapDetails.getGroupMembershipStrategy() != null) {
            T groupMembershipStrategy = useGroupMembershipStrategy(ldapDetails.getGroupMembershipStrategy());
            groupMembershipStrategy.configure(ldapDetails.getGroupMembershipStrategyParam());
        }
    }

    public static Matcher<LdapSecurityRealm> hasValidationSuccess() {
        return new BaseMatcher<LdapSecurityRealm>() {
            @Override
            public boolean matches(Object o) {
                LdapSecurityRealm realm = (LdapSecurityRealm) o;
                final String id = realm.validateButton.resolve().getAttribute("id");
                final By by = By.id(id + "_result");
                return realm.waitFor().withTimeout(30, TimeUnit.SECONDS).until(new Function<CapybaraPortingLayer, Boolean>() {
                            @Nullable
                            @Override
                            public Boolean apply(@Nullable CapybaraPortingLayer input) {
                                for (WebElement element : realm.all(by)) {
                                    if (element.findElements(By.tagName("div")).isEmpty()) {
                                        continue;
                                    }
                                    if (element.findElements(By.className("error")).isEmpty()
                                            && element.findElements(By.className("warning")).isEmpty()) {
                                        return Boolean.TRUE;
                                    } else {
                                        return Boolean.FALSE;
                                    }
                                }
                                return null;
                            }

                            @Override
                            public String toString() {
                                return "Validation result for: " + by;
                            }
                        });
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("LDAP Validation Test reports no warnings or errors");
            }
        };
    }

    public static Matcher<LdapSecurityRealm> hasValidationWarning() {
        return new BaseMatcher<LdapSecurityRealm>() {
            @Override
            public boolean matches(Object o) {
                LdapSecurityRealm realm = (LdapSecurityRealm) o;
                final String id = realm.validateButton.resolve().getAttribute("id");
                final By by = By.id(id + "_result");
                return realm.waitFor().withTimeout(30, TimeUnit.SECONDS).until(new Function<CapybaraPortingLayer, Boolean>() {
                            @Nullable
                            @Override
                            public Boolean apply(@Nullable CapybaraPortingLayer input) {
                                for (WebElement element : realm.all(by)) {
                                    if (element.findElements(By.tagName("div")).isEmpty()) {
                                        continue;
                                    }
                                    if (!element.findElements(By.className("warning")).isEmpty()) {
                                        return Boolean.TRUE;
                                    } else {
                                        return Boolean.FALSE;
                                    }
                                }
                                return null;
                            }

                            @Override
                            public String toString() {
                                return "Validation result for: " + by;
                            }
                        });
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("LDAP Validation Test reports at least one warning");
            }
        };
    }

    public static Matcher<LdapSecurityRealm> hasValidationError() {
        return new BaseMatcher<LdapSecurityRealm>() {
            @Override
            public boolean matches(Object o) {
                LdapSecurityRealm realm = (LdapSecurityRealm) o;
                final String id = realm.validateButton.resolve().getAttribute("id");
                final By by = By.id(id + "_result");
                return realm.waitFor().withTimeout(30, TimeUnit.SECONDS).until(new Function<CapybaraPortingLayer, Boolean>() {
                            @Nullable
                            @Override
                            public Boolean apply(@Nullable CapybaraPortingLayer input) {
                                for (WebElement element : realm.all(by)) {
                                    if (element.findElements(By.tagName("div")).isEmpty()) {
                                        continue;
                                    }
                                    if (!element.findElements(By.className("error")).isEmpty()) {
                                        return Boolean.TRUE;
                                    } else {
                                        return Boolean.FALSE;
                                    }
                                }
                                return null;
                            }

                            @Override
                            public String toString() {
                                return "Validation result for: " + by;
                            }
                        });
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("LDAP Validation Test reports at least one error");
            }
        };
    }

    public static class Validation extends CapybaraPortingLayerImpl {

        private final Control user;
        private final Control password;
        private final Control submit;

        public Validation(PageObject context, String id) {
            super(context.injector);
            user = new Control(context.injector, by.css("#%s input[name='%s']", id, "testUser"));
            password = new Control(context.injector, by.css("#%s input[name='%s']", id, "testPassword"));
            submit = new Control(context.injector, by.css("#%s .ldap-validate", id));
        }

        public void submit(String user, String password) {
            this.user.set(user);
            this.password.set(password);
            this.submit.click();
        }

    }

}
