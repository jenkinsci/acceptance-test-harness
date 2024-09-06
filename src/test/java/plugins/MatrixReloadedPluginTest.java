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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.matrix_reloaded.MatrixReloadedAction;
import org.jenkinsci.test.acceptance.po.MatrixBuild;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.MatrixRun;
import org.jenkinsci.test.acceptance.po.TextAxis;
import org.junit.Test;

@WithPlugins({
    "matrix-project", // JENKINS-37545
    "matrix-reloaded"
})
public class MatrixReloadedPluginTest extends AbstractJUnitTest {

    @Test
    public void rebuild_one_combination() {
        MatrixProject job = jenkins.jobs.create(MatrixProject.class);
        job.configure();
        TextAxis x = job.addAxis(TextAxis.class);
        x.name.set("AAA");
        x.valueString.set("111 222");

        TextAxis y = job.addAxis(TextAxis.class);
        y.name.set("BBB");
        y.valueString.set("333 444");
        job.save();
        job.startBuild().waitUntilFinished();

        MatrixReloadedAction action = job.getLastBuild().action(MatrixReloadedAction.class);
        action.open();

        action.shouldHaveCombination("AAA=111,BBB=333")
                .shouldHaveCombination("AAA=111,BBB=444")
                .shouldHaveCombination("AAA=222,BBB=333")
                .shouldHaveCombination("AAA=222,BBB=444");

        action.rebuild("AAA=111,BBB=333");
        MatrixBuild build = (MatrixBuild) job.getLastBuild().shouldSucceed();

        assertThat(build.getConfiguration("AAA=111,BBB=333"), exists());
        assertThat(build.getConfiguration("AAA=111,BBB=444"), not(exists()));
        assertThat(build.getConfiguration("AAA=222,BBB=333"), not(exists()));
        assertThat(build.getConfiguration("AAA=222,BBB=444"), not(exists()));
    }

    private Matcher<? super MatrixRun> exists() {
        return new Matcher<>("Matrix run exists") {
            @Override
            public boolean matchesSafely(MatrixRun item) {
                return item.exists();
            }
        };
    }
}
