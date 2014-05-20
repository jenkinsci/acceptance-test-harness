/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
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
package org.jenkinsci.test.acceptance.plugins.active_directory;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.NoSuchElementException;

/**
 * Page Object for AD security (global) configuration page.
 * @author Marco Miller
 */
public class ActiveDirectorySecurity extends PageObject {

    private static String bindDNEnv;
    private static String controllerEnv;
    private static String domainEnv;
    private static String passwordEnv;
    private static String siteEnv;
    private static String userEnv;

    private static final String sec = "/useSecurity";
    private static final String realm = sec+"/realm[0]";
    private static final String authPrefix = sec+"/authorization[";

    public final Jenkins jenkins;
    public final Control use = control(sec);
    public final Control aD = control(realm);
    public final Control domain = control(realm+"/domain");
    public final Control advanced = control(realm+"/advanced-button");
    public final Control dController = control(realm+"/server");
    public final Control site = control(realm+"/site");
    public final Control pwd = control(realm+"/bindPassword");
    public final Control dn = control(realm+"/bindName");
    public final Control save = control("/Submit");
    public Control auth;

    public ActiveDirectorySecurity(Jenkins jenkins) {
        super(jenkins.injector,jenkins.url("configureSecurity"));
        this.jenkins = jenkins;
        getEnvs();
    }

    /**
     * @return true if successfully saved AD security configuration, false otherwise-
     */
    public boolean successfullySaveSecurityConfig() {
        open();
        if(!use.resolve().isSelected()) use.click();
        if(!aD.resolve().isSelected()) aD.click();
        domain.set(domainEnv);
        advanced.click();
        if(controllerEnv != null) dController.set(controllerEnv);
        if(siteEnv != null) site.set(siteEnv);
        pwd.set(passwordEnv);
        dn.set(bindDNEnv);

        int index = 0;
        boolean firstMatrixBasedFound = false;
        while(!firstMatrixBasedFound) {
            auth = control(authPrefix+index+"]");
            auth.click();
            auth = control(authPrefix+index+"]/");
            try {
                auth.resolve();
                firstMatrixBasedFound = true;
                auth.set(userEnv);
                clickButton("Add");
                auth = control(authPrefix+index+"]/data/"+userEnv+"/hudson.model.Hudson.Administer");
                if(!auth.resolve().isSelected()) auth.click();
            }
            catch(NoSuchElementException e) {
                index++;
            }
        }
        save.click();
        doLoginDespiteNoPathsThenWaitForLdap();
        open();
        boolean succeeded = true;
        try {
            auth.resolve();
            if(use.resolve().isSelected()) {
                use.click();
                save.click();
            }
            else succeeded = false;
        }
        catch(NoSuchElementException e) {
            succeeded = false;
        }
        return succeeded;
    }

    private void doLoginDespiteNoPathsThenWaitForLdap() {
        jenkins.login();
        driver.findElement(by.name("j_username")).sendKeys(userEnv);
        driver.findElement(by.name("j_password")).sendKeys(passwordEnv);
        clickButton("log in");
    }

    private void getEnvs() {
        bindDNEnv = ActiveDirectoryEnv.getInstance().getBindDN();
        controllerEnv = ActiveDirectoryEnv.getInstance().getController();
        domainEnv = ActiveDirectoryEnv.getInstance().getDomain();
        passwordEnv = ActiveDirectoryEnv.getInstance().getPassword();
        siteEnv = ActiveDirectoryEnv.getInstance().getSite();
        userEnv = ActiveDirectoryEnv.getInstance().getUser();
    }
}
