package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapConfig;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;

/**
 * PageObject for global security configuration.
 *
 * @author Michael Prankl
 */
public class SecurityConfig extends PageObject {

    private final Control cUseSecurity = control("/useSecurity");
    private final Control cSave = control("/Submit");
    private final Control cApply = control("/Apply");


    public SecurityConfig(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("configureSecurity/"));
    }

    private void configureLdap(LdapDetails ldapDetails) {
        cUseSecurity.check(true);
        LdapConfig l = new LdapConfig(this);
        l.enterLdapDetails(ldapDetails);
    }

    /**
     * Configures global security to use the specified ldap server for access control and saves the changes.
     */
    public void configureLdapAndSave(LdapDetails ldapDetails) {
        this.configureLdap(ldapDetails);
        cSave.click();
    }

    /**
     * Configures global security to use the specified ldap server for access control and applies the changes.
     */
    public void configureLdapAndApply(LdapDetails ldapDetails) {
        this.configureLdap(ldapDetails);
        cApply.click();
    }
}
