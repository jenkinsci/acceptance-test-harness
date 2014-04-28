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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;

/**
 * @author Marco.Miller@ericsson.com
 */
@WithPlugins("gerrit-trigger")
public class GerritTriggerTest extends AbstractJUnitTest {

    /**
     * Scenario: Gerrit has its Change review flags checked after Jenkins set them
     * Given a Jenkins instance that is either test-default or type=existing
     *  And a gerrit-trigger plugin that is either test-default or pre-installed
     *  And an existing Gerrit instance configured in that Jenkins
     * When I push a Change that builds successfully for review
     * Then Jenkins does build it successfully indeed
     *  And Jenkins sets the Change review flags accordingly towards Gerrit
     *  And Gerrit then consider these flags as checked
     */
    @Test
    public void gerrit_has_review_flags_checked_after_jenkins_set_them() {
        assertNotNull(jenkins.open());
        assertTrue(jenkins.getPluginManager().isInstalled("gerrit-trigger"));
        //TODO work in progress..
    }
}
