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
package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.gerrit_trigger.GerritTriggerJob;
import org.jenkinsci.test.acceptance.plugins.gerrit_trigger.GerritTriggerNewServer;
import org.jenkinsci.test.acceptance.plugins.gerrit_trigger.GerritTriggerServer;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 * @author Marco.Miller@ericsson.com
 */
@WithPlugins("gerrit-trigger")
public class GerritTriggerTest extends AbstractJUnitTest {

    /**
     * Scenario: Gerrit has its Change review flags checked after Jenkins set them-<br>
     * Given a Jenkins instance that is either test-default or type=existing<br>
     *  And a gerrit-trigger plugin that is either test-default or pre-installed<br>
     *  And an existing Gerrit instance configured in that Jenkins<br>
     * When I push a Change that builds successfully for review<br>
     * Then Jenkins does build it successfully indeed<br>
     *  And Jenkins sets the Change review flags accordingly towards Gerrit<br>
     *  And Gerrit then consider these flags as checked.
     */
    @Test
    public void gerrit_has_review_flags_checked_after_jenkins_set_them() {
        GerritTriggerNewServer newServer = new GerritTriggerNewServer(jenkins);
        newServer.saveNewTestServerConfigIfNone();
        GerritTriggerServer server = new GerritTriggerServer(jenkins);
        server.saveTestServerConfig();

        String jobName = this.getClass().getCanonicalName();
        jenkins.jobs.create(FreeStyleJob.class,jobName);
        GerritTriggerJob job = new GerritTriggerJob(jenkins,jobName);
        job.saveTestJobConfig();
        //TODO work in progress
    }
}
