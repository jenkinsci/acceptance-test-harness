/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;

@Describable({"Jenkins’ own user database", "Jenkins’s own user database"})
public class JenkinsDatabaseSecurityRealm extends SecurityRealm {

    public JenkinsDatabaseSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    public void allowUsersToSignUp(boolean allow) {
        control("allowsSignup").check(allow);
    }

    public Signup signup() {
        Signup signup = new Signup(getPage().getJenkins());
        signup.open();
        return signup;
    }

    public User signup(String name) {
        return signup().password(name).fullname(name).email(name + "@mailinator.com", false).signup(name);
    }

    public User signup(String name, String pwd, String fullName, String email) {
        return signup().password(pwd).fullname(fullName).email(email).signup(name);
    }

    public static final class Signup extends PageObject {

        protected Signup(Jenkins context) {
            super(context, context.url("signup"));
        }

        public Signup password(String pwd) {
            control(by.input("password1")).set(pwd);
            return this;
        }

        public Signup fullname(String name) {
            control(by.input("fullname")).set(name);
            return this;
        }

        /**
         * Configure the users email address 
         * @param mail the users email address
         * @param required true if the email address is required (the field dependes on the present of {@code mailer} plugin.
         */
        public Signup email(String mail, boolean required) {
            try {
                control(by.input("email")).set(mail);
            } catch (NoSuchElementException ex) {
                if (required) {
                    throw new AssertionError("email field requires mailer plugin installed", ex);
                }
            }
            return this;
        }

        public Signup email(String mail) {
            return email(mail, true);
        }

        public User signup(String name) {
            control(by.input("username")).set(name);
            control(by.name("Submit")).clickAndWaitToBecomeStale();

            return new User(getJenkins(), name);
        }
    }
}
