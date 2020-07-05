package org.jenkinsci.test.acceptance.plugins.saml;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.SecurityRealm;

@Describable("SAML 2.0")
public class SamlSecurityRealm extends SecurityRealm {
    private final Control displayNameAttr = control("displayNameAttributeName");
    private final Control userNameAttr = control("usernameAttributeName");
    private final Control groupsAttr = control("groupsAttributeName");
    private final Control emailAttr = control("emailAttributeName");

    private final Control advancedConfig = control("advancedConfiguration");
    private final Control spEntityId = control("advancedConfiguration/spEntityId");
    private final Control IdpMetadataConfiguration = control("idpMetadataConfiguration");
    private final Control xml = control("idpMetadataConfiguration/xml");
    private final Control url = control("idpMetadataConfiguration/url");
    private final Control period = control("IdpMetadataConfiguration/period");

    private final Control encryption = control("encryptionData");
    private final Control keyStorePath = control("encryptionData/keystorePath");
    private final Control keyStorePassword = control("encryptionData/keystorePassword");
    private final Control privateKeyPassword = control("encryptionData/privateKeyPassword");

    private final Control binding = control("binding");

    public SamlSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    public void setDisplayNameAttribute(String value) {
        displayNameAttr.set(value);
    }
    public void setUserNameAttribute(String value) {
        userNameAttr.set(value);
    }
    public void setGroupsAttribute(String value) {
        groupsAttr.set(value);
    }
    public void setEmailAttribute(String value) {
        emailAttr.set(value);
    }
    public void advancedConfig() {
        advancedConfig.click();
        // wait a bit for the advanced section to render
        // "Force Authentication" is one of its elements
        waitFor(by.xpath("//span[.='Force Authentication'] | //td[.='Force Authentication']"), 2);
    }
    public void setSpEntityIdAttribute(String value) {
        spEntityId.set(value);
    }
    public void setXml(String value) {
        xml.set(value);
    }
    public void setUrl(String value) {
        url.set(value);
    }
    public void encryptionConfig() {
        encryption.click();
        // wait a bit for the encryption section to render
        // "Keystore path" is one of its elements
        waitFor(by.xpath("//div[.='Keystore path'] | //td[.='Keystore path']"), 2);
    }
    public void setKeyStorePath(String value) {
        keyStorePath.set(value);
    }
    public void setKeyStorePassword(String value) {
        keyStorePassword.set(value);
    }
    public void setPrivateKeyPassword(String value) {
        privateKeyPassword.set(value);
    }
    public void setBinding(String value){
        binding.select(value);
    }
}
