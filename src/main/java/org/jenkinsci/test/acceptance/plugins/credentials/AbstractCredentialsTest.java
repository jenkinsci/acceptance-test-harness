/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.plugins.credentials;

import java.io.File;
import java.util.Locale;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.openqa.selenium.WebElement;

public class AbstractCredentialsTest extends AbstractJUnitTest {

    private static final long ENTROPY = System.currentTimeMillis();

    public static final String CREATED_USER = "myuser" + ENTROPY;

    public static final String CRED_ID = "credid";
    public static final String CRED_USER = "myuser" + ENTROPY;
    public static final String CRED_PWD = "mypass";
    public static final String CRED_DSCR = "username password credentials";
    public static final String SECRET_TEXT = "secret text";
    public static final String SECRET_FILE = "secretFile";
    public static final String SECRET_FILE_TEXT = "secret inside a file";
    public static final String SECRET_ZIP_FILE = "secretZip.zip";
    public static final String SECRET_ZIP_FILE_TEXT = "secret content inside zip file";
    public static final String GLOBAL_SCOPE = "GLOBAL";
    public static final String SYSTEM_SCOPE = "SYSTEM";

    protected void createMockUserAndLogin() {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();

        final JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        security.save();

        // create the user and sign in
        realm.signup(CRED_USER);
        jenkins.login().doLogin(CRED_USER);
    }

    protected CredentialsPage createCredentialsPage(Boolean userCredentials)  {
        return createCredentialsPage(userCredentials, ManagedCredentials.DEFAULT_DOMAIN);
    }

    protected CredentialsPage createCredentialsPage(Boolean userCredentials, String domain)  {
        CredentialsPage cp = null;
        if (userCredentials) {
            cp = new CredentialsPage(jenkins, domain, CREATED_USER);
            //Make sure we are not going to have a 404, we got some issues like that
            navigateToCreateCredentials();
        } else {
            cp = new CredentialsPage(jenkins, domain);
            cp.open();
        }
        return cp;
    }

    protected  <T extends BaseStandardCredentials> T createCredentials(Class<T> credClazz, CredentialsPage cp, String scope) {
        return createCredentials(credClazz,cp, scope, SECRET_FILE);
    }

    protected <T extends BaseStandardCredentials> T createCredentials(Class<T> credClazz, CredentialsPage cp, String scope, String file) {
        final T cred = cp.add(credClazz);

        if (UserPwdCredential.class.equals(credClazz)) {
            final UserPwdCredential castedCred = (UserPwdCredential) cred;
            castedCred.username.set(CRED_USER);
            castedCred.password.set(CRED_PWD);
            castedCred.description.set(CRED_DSCR);
        } else if (StringCredentials.class.equals(credClazz)) {
            final StringCredentials castedCred = (StringCredentials) cred;
            castedCred.secret.set(SECRET_TEXT);
            castedCred.description.set(CRED_DSCR);
        } else if (FileCredentials.class.equals(credClazz)) {
            final FileCredentials castedCred = (FileCredentials) cred;
            castedCred.description.set(CRED_DSCR);
            final WebElement we = castedCred.file.resolve();
            Class<? extends AbstractCredentialsTest> kl = getClass();
            String dir = kl.getSimpleName().toLowerCase(Locale.ENGLISH).replace("test", "");
            final File fileToUpload = new File(kl.getResource("/" + dir + "/" + file).getFile());
            we.sendKeys(fileToUpload.getAbsolutePath());
        } else if (SshPrivateKeyCredential.class.equals(credClazz)) {
            SshPrivateKeyCredential castedCred = (SshPrivateKeyCredential) cred;
            castedCred.description.set(CRED_DSCR);
            if (scope != null) {
                castedCred.scope.select(scope);
            }
            castedCred.username.set(CRED_USER);
            castedCred.selectEnterDirectly().privateKey.set(CRED_PWD);
        }

        cred.setId(CRED_ID);
        cp.create();
        return cred;
    }

    private void navigateToCreateCredentials() {
        jenkins.visit("user/" + CREATED_USER);
        tryCredentialsClick();
        waitFor(by.href("/user/" + CREATED_USER + "/credentials/store/user")).click();
        waitFor(by.href("domain/_")).click();
        waitFor(by.href("newCredentials")).click();
        waitFor(by.name("_.id"));
    }

    private void tryCredentialsClick() {
        WebElement credentials = getElement(by.xpath("//a[contains(@href, '/user/" + CREATED_USER + "/credentials') and contains(@class, 'task-link')]"));
        if (credentials == null) {
            // Somehow login has been lost (we have found this problem on rosie) so we try to re login again
            reTryLogin();
            credentials = waitFor(by.href("/user/" + CREATED_USER + "/credentials"));
        }
        credentials.click();
    }

    private void reTryLogin() {
        jenkins.login().doLogin(CRED_USER);
    }

}
