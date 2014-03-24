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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

public class MavenPluginTest extends AbstractJUnitTest {

    private static final String GENERATE = "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X";

    @Test
    public void autoinstall_maven_for_freestyle_job() {
        install_maven("maven_3.0.4", "3.0.4");

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenStep step = job.addBuildStep(MavenStep.class);
        step.version.select("maven_3.0.4");
        step.targets.set(GENERATE);
        job.save();

        job.queueBuild().shouldSucceed()
                .shouldContainsConsoleOutput("Apache Maven 3.0.4")
                .shouldContainsConsoleOutput("Unpacking http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip")
        ;
    }

    @Test
    public void autoinstall_maven2_for_freestyle_job() {
        install_maven("maven_2.2.1", "2.2.1");

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenStep step = job.addBuildStep(MavenStep.class);
        step.version.select("maven_2.2.1");
        step.targets.set(GENERATE);
        job.save();

        job.queueBuild().shouldSucceed()
                .shouldContainsConsoleOutput("Apache Maven 2.2.1")
                .shouldContainsConsoleOutput("Unpacking http://archive.apache.org/dist/maven/binaries/apache-maven-2.2.1-bin.zip")
        ;
    }

    @Test @Native("mvn")
    public void use_native_maven() {
        jenkins.configure();
        MavenInstallation maven = jenkins.getConfigPage().addTool(MavenInstallation.class);
        maven.name.set("native_maven");
        maven.useNative();
        jenkins.save();

        String expectedVersion = localMavenVersion();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenStep step = job.addBuildStep(MavenStep.class);
        step.version.select("native_maven");
        step.targets.set("--version");
        job.save();

        Build build = job.queueBuild().shouldSucceed();

        build.shouldContainsConsoleOutput(Pattern.quote(expectedVersion));
    }

    private String localMavenVersion() {
        final Pattern pattern = Pattern.compile("Apache Maven .*");
        final Matcher matcher = pattern.matcher(jenkins.runScript("'mvn --version'.execute().text"));
        matcher.find();
        return matcher.group(0);
    }

    @Test
    public void use_local_maven_repo() {
        install_some_maven();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenStep step = job.addBuildStep(MavenStep.class);
        step.targets.set(GENERATE);
        step.useLocalRepository();
        job.save();

        job.queueBuild().shouldSucceed().shouldContainsConsoleOutput("-Dmaven.repo.local=([^\\n]*)/.repository");
    }

    private void install_some_maven() {
        install_maven("default_maven", "3.0.5");
    }

    private void install_maven(String name, String version) {
        jenkins.configure();
        MavenInstallation maven = jenkins.getConfigPage().addTool(MavenInstallation.class);
        maven.name.set(name);
        maven.installVersion(version);
        jenkins.save();
    }
}
