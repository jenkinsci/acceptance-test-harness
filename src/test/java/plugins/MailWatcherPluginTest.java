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
package plugins;

import com.google.inject.Inject;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.docker.fixtures.MailhogContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mail_watcher.OnlineStatusNotification;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.utils.mail.MailhogProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;

@WithPlugins("mail-watcher-plugin")
@Category(DockerTest.class)
@WithDocker
public class MailWatcherPluginTest extends AbstractJUnitTest {

    @Inject
    SlaveController slaveController;

    @Inject
    MailhogProvider mailhogProvider;
    private MailhogContainer mailhog;

    @Before
    public void setUp() {
        mailhog = mailhogProvider.get();
    }

    @Test
    public void notify_slave_on_restart() throws Exception {
        Future<Slave> futureSlave = slaveController.install(jenkins);

        Slave slave = futureSlave.get();
        slave.configure();
        {
            OnlineStatusNotification notification = new OnlineStatusNotification(slave);
            notification.onOnline("on@online.com");
            notification.onOffline("on@offline.com");
        }
        slave.save();

        jenkins.restart();

        mailhog.assertMail(regex("Computer %s marked offline", slave.getName()), "on@offline.com");
        mailhog.assertMail(regex("Computer %s marked online", slave.getName()), "on@online.com");
    }

    @Test @Issue("JENKINS-20538") @Since("1.571") @WithPlugins("mail-watcher-plugin")
    public void notify_master_on_jenkins_restart() throws Exception {
        jenkins.configure();
        {
            OnlineStatusNotification notification = new OnlineStatusNotification(jenkins);
            notification.onOnline("on@online.com");
            notification.onOffline("on@offline.com");
        }
        jenkins.save();

        jenkins.restart();

        mailhog.assertMail(regex("Computer master marked offline"), "on@offline.com", regex("Jenkins is restarting"));
        mailhog.assertMail(regex("Computer master marked online"), "on@online.com");
    }

    private Pattern regex(String expression, Object... args) {
        return Pattern.compile(String.format(expression, args));
    }
}
