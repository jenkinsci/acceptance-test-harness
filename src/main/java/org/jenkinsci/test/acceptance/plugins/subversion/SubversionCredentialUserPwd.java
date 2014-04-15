package org.jenkinsci.test.acceptance.plugins.subversion;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;

import java.net.URL;

/**
 * Created by karl on 4/15/14.
 */
public class SubversionCredentialUserPwd extends SubversionCredential {
    private static final String RADIO_BUTTON_NAME = "password";
    private static final String INPUT_USERNAME = "username1";
    private static final String INPUT_PASSWORD = "password1";


    public SubversionCredentialUserPwd(Injector injector, URL url, String parentWindowHandle) throws Exception {
        super(injector, url, parentWindowHandle);

        super.switchHandle(super.getWindowHandle());
        find(by.radioButton(RADIO_BUTTON_NAME)).click();
        super.switchHandle(super.getParentWindowHandle());
    }

    public void setPassword(String password) {
        super.switchHandle(super.getWindowHandle());

        Control pwdField = control(by.input(INPUT_PASSWORD));
        pwdField.set(password);

        super.switchHandle(super.getParentWindowHandle());
    }

    public void setUsername(String username) {
        super.switchHandle(super.getWindowHandle());

        Control userField = control(by.input(INPUT_USERNAME));
        userField.set(username);

        super.switchHandle(super.getParentWindowHandle());
    }
}
