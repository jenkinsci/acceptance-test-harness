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


    public SecurityConfig(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("configureSecurity/"));
    }

    /**
     * Configures global security to use the specified ldap server for access control.
     */
    public void configureLdap(LdapDetails ldapDetails){
        cUseSecurity.check(true);
        LdapConfig l = new LdapConfig(this);
        l.enterLdapDetails(ldapDetails);
        cSave.click();
    }
}
