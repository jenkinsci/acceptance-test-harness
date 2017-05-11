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
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.groovy.GroovyInstallation;
import org.jenkinsci.test.acceptance.plugins.groovy.GroovyStep;
import org.jenkinsci.test.acceptance.plugins.groovy.SystemGroovyStep;
import org.jenkinsci.test.acceptance.plugins.script_security.ScriptApproval;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.junit.Test;

@WithPlugins("groovy")
public class GroovyPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Test
    public void run_groovy() {
        GroovyInstallation.installSomeGroovy(jenkins);
        configureJob();

        createDefaultGroovyBuildStep().script(
                "println 'running groovy script';"
        );

        shouldReport("running groovy script");
    }

    @Test
    public void run_groovy_from_file() {
        GroovyInstallation.installSomeGroovy(jenkins);
        configureJob();

        job.addShellStep("echo println \\'running groovy file\\' > script.groovy");
        createDefaultGroovyBuildStep().file("script.groovy");

        shouldReport("running groovy file");
    }

    private GroovyStep createDefaultGroovyBuildStep() {
        GroovyStep groovyStep = job.addBuildStep(GroovyStep.class);
        groovyStep.version.select(GroovyInstallation.DEFAULT_GROOVY_ID);
        return groovyStep;
    }

    @Test
    public void run_system_groovy() {
        configureJob();

        job.addBuildStep(SystemGroovyStep.class).script(
                "job = jenkins.model.Jenkins.instance.getJob('my_job');" +
                "println \"name: ${job.displayName}. number: ${job.lastBuild.number}\""
        , false);

        shouldReport("name: my_job. number: 1");
    }

    @Test
    public void run_system_groovy_from_file() {
        configureJob();

        job.addShellStep("echo println \\'running groovy file\\' > script.groovy");
        job.addBuildStep(SystemGroovyStep.class).file("script.groovy");

        /* TODO cf. FileSystemScriptSourceTest.smokes; when added to generic-whitelist, simplify to:
        shouldReport("running groovy file");
        */
        job.save();
        Build build = job.startBuild();
        if (build.isSuccess()) {
            build.shouldContainsConsoleOutput("running groovy file");
        } else {
            build.shouldContainsConsoleOutput("org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use method groovy.lang.Script println java.lang.Object");
            ScriptApproval sa = new ScriptApproval(jenkins);
            sa.open();
            sa.findSignature("method groovy.lang.Script println java.lang.Object").approve();
            job.startBuild().shouldSucceed().shouldContainsConsoleOutput("running groovy file");
        }
    }

    @Test
    public void use_custom_groovy_version() {
        GroovyInstallation.installGroovy(jenkins, "groovy-2.2.1", "Groovy 2.2.1");

        configureJob();

        final GroovyStep step = job.addBuildStep(GroovyStep.class);
        step.version.select("groovy-2.2.1");
        step.script(
                "println 'version: ' + groovy.lang.GroovySystem.getVersion()"
        );

        shouldReport("version: 2.2.1");
    }

    @Test @Native("groovy")
    public void use_native_groovy() {
        GroovyInstallation groovy = ToolInstallation.addTool(jenkins, GroovyInstallation.class);
        groovy.name.set("local-groovy");
        groovy.useNative();
        groovy.getPage().save();

        configureJob();
        final GroovyStep step = job.addBuildStep(GroovyStep.class);
        step.version.select("local-groovy");
        step.script(
                "println 'version: ' + groovy.lang.GroovySystem.getVersion()"
        );
        job.save();
        Build build = job.startBuild().shouldSucceed();

        String expectedVersion = localGroovyVersion();

        build.shouldContainsConsoleOutput("version: " + expectedVersion);
    }

    private void configureJob() {
        job = jenkins.jobs.create(FreeStyleJob.class, "my_job");
        job.configure();
    }

    private void shouldReport(String out) {
        job.save();
        job.startBuild().shouldSucceed().shouldContainsConsoleOutput(out);
    }

    private String localGroovyVersion() {
        final Pattern pattern = Pattern.compile("Groovy Version: (.+) JVM:");
        final Matcher matcher = pattern.matcher(jenkins.runScript("'groovy --version'.execute().text"));
        matcher.find();
        return matcher.group(1);
    }
}
