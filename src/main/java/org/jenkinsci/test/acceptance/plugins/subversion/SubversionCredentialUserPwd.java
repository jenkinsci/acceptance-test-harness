package org.jenkinsci.test.acceptance.plugins.subversion;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;

import java.net.URL;

/**
 * Class for the authentication method username/password in the SVN credential page.
 *
 * @author Matthias Karl
 */
public class SubversionCredentialUserPwd extends SubversionCredential {
    private static final String RADIO_BUTTON_NAME = "password";
    private static final String INPUT_USERNAME = "username1";
    private static final String INPUT_PASSWORD = "password1";


    public SubversionCredentialUserPwd(Injector injector, URL url, String parentWindowHandle) throws Exception {
        super(injector, url, parentWindowHandle);

        super.switchToPopupHandle();
        find(by.radioButton(RADIO_BUTTON_NAME)).click();
        super.switchToParentHandle();
    }

    public void setPassword(String password) {
        super.switchToPopupHandle();

        Control pwdField = control(by.input(INPUT_PASSWORD));
        pwdField.set(password);

        super.switchToParentHandle();
    }

    public void setUsername(String username) {
        super.switchToPopupHandle();

        Control userField = control(by.input(INPUT_USERNAME));
        userField.set(username);

        super.switchToParentHandle();
    }
}
