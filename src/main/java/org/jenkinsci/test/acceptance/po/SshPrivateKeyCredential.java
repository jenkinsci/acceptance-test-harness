package org.jenkinsci.test.acceptance.po;

import java.net.URL;

/**
 * @author Vivek Pandey
 */
public class SshPrivateKeyCredential extends Credential {
    public SshPrivateKeyCredential(Jenkins j) {
        super(j, j.url("credentials"));
    }
    /**
     * Create Credential
     * @param scope Either of "GLOBAL" or "SYSTEM"
     */
    public void create(String scope, String username, String privateKey){
        if(!scope.equals("GLOBAL") && !scope.equals("SYSTEM")){
            throw new AssertionError("Credential scope must be either of GLOBAL or SYSTEM");
        }
        selectDropdownMenu("SSH Username with private key",find(by.path("/domainCredentials/hetero-list-add[credentials]")));
        find(by.input("_.description")).sendKeys("SSH Key setup");
        find(by.input("_.username")).sendKeys(username);
        find(by.input("_.privateKey")).sendKeys(privateKey);
        clickButton("Save");
    }
}
