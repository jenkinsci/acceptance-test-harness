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
package core;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Cookie;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.WebDriver.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@WithPlugins("mailer")
public class JenkinsDatabaseSecurityRealmTest extends AbstractJUnitTest {

    private static final String EMAIL = "jatu@gmail.com";
    private static final String FULL_NAME = "Full Name";
    private static final String PWD = "4242";
    private static final String NAME = "jenkins-acceptance-tests-user";
    
    private JenkinsDatabaseSecurityRealm realm;

    @Before
    public void setUp() {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        security.save();
    }

    static void addSessionCookie(Options manager, String path, Date date) {
        manager.addCookie(new Cookie("JSESSIONID."+Integer.toHexString(new Random().nextInt()),
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                path,
                date));
    }

    @Test
    public void many_sessions_logout() {
        User user = realm.signup().fullname(FULL_NAME).email(EMAIL).password(PWD).signup(NAME);

        jenkins.login().doLogin(user.id(), PWD);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        byte[] array = new byte[7];
        Options manager = driver.manage();
        Collections.nCopies(8, 1)
                .stream()
                .forEach(i -> addSessionCookie(manager, "/", tomorrow));
        addSessionCookie(manager, "/will-not-be-sent", tomorrow);

        jenkins.logout();

        StringBuilder builder = new StringBuilder();
        int sessionCookies = 0;

        final boolean debugCookies = false;
        for (Cookie cookie : manager.getCookies()) {
            if (cookie.getName().startsWith("JSESSIONID.")) {
                ++sessionCookies;

                if (debugCookies) {
                    builder.append(cookie.getName());
                    String path = cookie.getPath();
                    if (path != null)
                        builder.append("; Path=").append(path);
                    builder.append("\n");
                }
            }
        }
        if (debugCookies) {
            assertThat(builder.toString(), is(equalTo("")));
        }
        assertThat(sessionCookies, is(equalTo(1)));
    }

    @Test
    //TODO Re enable category once JENKINS-49524 is done
    //@Category(SmokeTest.class)
    public void login_and_logout() {

        User user = realm.signup().fullname(FULL_NAME).email(EMAIL).password(PWD).signup(NAME);

        jenkins.login().doLogin(user.id(), PWD);

        assertEquals(user, jenkins.getCurrentUser());

        jenkins.logout();

        assertEquals(null, jenkins.getCurrentUser().id());
    }

    @Test
    public void create_update_delete() {

        User user = realm.signup().fullname(FULL_NAME).password(PWD).email(EMAIL).signup(NAME);
        assertThat(user.id(), equalTo(NAME));
        assertThat(user.fullName(), equalTo(FULL_NAME));
        jenkins.logout();

        user.configure();
        user.fullName("ASDF");
        user.save();
        user = jenkins.getUser(NAME);

        assertThat(user.id(), equalTo(NAME));
        assertThat(user.fullName(), equalTo("ASDF"));

        user.delete();
        user = jenkins.getUser(NAME);
        try {
            assertThat(user, Matchers.pageObjectDoesNotExist());
        } catch (AssertionError ex) {
            // Old Jenkins creates new users transparently. Verifying it is the new one and not the old by default fullName assigned
            assertThat(user.fullName(), equalTo(NAME));
        }
    }
}
