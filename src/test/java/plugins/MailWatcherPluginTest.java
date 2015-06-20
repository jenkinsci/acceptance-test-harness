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

import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mail_watcher.OnlineStatusNotification;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.utils.mail.MailService;
import org.junit.Test;

import com.google.inject.Inject;

@WithPlugins("mail-watcher-plugin")
public class MailWatcherPluginTest extends AbstractJUnitTest {

    @Inject
    private MailService mail;

    @Inject
    SlaveController slaveController;

    @Test
    public void notify_slave_on_restart() throws Exception {
        Future<Slave> futureSlave = slaveController.install(jenkins);

        mail.setup(jenkins);

        Slave slave = futureSlave.get();
        slave.configure();
        {
            OnlineStatusNotification notification = new OnlineStatusNotification(slave);
            notification.onOnline("on@online.com");
            notification.onOffline("on@offline.com");
        }
        slave.save();

        jenkins.restart();

        mail.assertMail(regex("Computer %s marked offline", slave.getName()), "on@offline.com");
        mail.assertMail(regex("Computer %s marked online", slave.getName()), "on@online.com");
    }

    @Test @Issue("JENKINS-20538") @Since("1.571") @WithPlugins("mail-watcher-plugin@1.7")
    public void notify_master_on_jenkins_restart() throws Exception {
        mail.setup(jenkins);

        jenkins.configure();
        {
            OnlineStatusNotification notification = new OnlineStatusNotification(jenkins);
            notification.onOnline("on@online.com");
            notification.onOffline("on@offline.com");
        }
        jenkins.save();

        jenkins.restart();

        mail.assertMail(regex("Computer master marked offline"), "on@offline.com", regex("Jenkins is restarting"));
        mail.assertMail(regex("Computer master marked online"), "on@online.com");
    }

    private Pattern regex(String expression, Object... args) {
        return Pattern.compile(String.format(expression, args));
    }
}
