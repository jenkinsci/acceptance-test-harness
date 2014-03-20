package org.jenkinsci.test.acceptance.po;


/**
 * @author Vivek Pandey
 */
public class SshPrivateKeyCredential extends Credential {
    public SshPrivateKeyCredential(Jenkins j) {
        super(j, j.url("credentials/"));
    }
    /**
     * Create Credential
     * @param scope Either of "GLOBAL" or "SYSTEM"
     */
    public void create(String scope, String username, String privateKey){
        if(!scope.equals("GLOBAL") && !scope.equals("SYSTEM")){
            throw new AssertionError("Credential scope must be either of GLOBAL or SYSTEM");
        }
        visit(url);
        if(exists(username,privateKey)){
            return;
        }
        find(by.input("_.description")).sendKeys("SSH Key setup");
        find(by.input("_.username")).clear(); //it's always pre-filled with system default user
        find(by.input("_.username")).sendKeys(username);
        find(by.input("_.privateKey")).sendKeys(privateKey);
        clickButton("Save");
    }

    private boolean exists(String username, String privatekey){
        selectDropdownMenu("SSH Username with private key", find(by.path("/domainCredentials/hetero-list-add[credentials]")));
        String foundUsername = find(by.input("_.username")).getAttribute("value");
        String foundPrivateKey = find(by.input("_.privateKey")).getText();
        return foundUsername != null && foundUsername.equals(username)
                && foundPrivateKey != null && foundPrivateKey.equals(privatekey.replace('\n',' ').trim());

    }
}
