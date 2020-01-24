/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.utils;

import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.utils.process.CommandBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main class to run ATH test maven module inside ATH container.
 *
 * @author ogondza.
 */
public final class ContainerizedRunner {
    private static final Logger LOGGER = Logger.getLogger(ContainerizedRunner.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {

        Docker d = new Docker(); // static fields initialized in constructor :-/

        if (!d.isAvailable()) {
            // TODO, buildPlugin() have no way to pass in the maven opts to disable this module selectively on where desired. Lets just suppress the failure to verify the concept and work on that later
            LOGGER.warning("Unable to run ATH tests in docker as it is not installed or running");
            System.exit(0);
        }

        // TODO either accept from the target plugin or bake in the ATH version when released
        String tag = "jenkins/ath:acceptance-test-harness-1.69";
        LOGGER.info("Pulling image " + tag);
        assert d.cmd("pull", tag).system() == 0;

        //String athModuleSources = System.getProperty("user.dir");

        CommandBuilder cmd = d.cmd(
                "run", "--rm",
                "--user=ath-user",
                "--workdir=/home/ath-user/sources",
                "-v=/var/run/docker.sock:/var/run/docker.sock",
                "-v=" + System.getenv("MAVEN_PROJECTBASEDIR") + ":/home/ath-user/sources",
                //"-v=" + System.getenv("HOME") + "/.m2/repository:/home/ath-user/.m2/repository",
                "--shm-size=2g",
                "-e", "JENKINS_VERSION=" + System.getenv("JENKINS_VERSION"),
                "-e", "LOCAL_JARS=" + System.getenv("LOCAL_JARS"),
                "-e", "FORM_ELEMENT_PATH_VERSION=1.6",
                tag,
                "bash", "-c", "eval $(vnc.sh); mvn -B package -pl=ui-tests -Dmaven.test.skip=false" // TODO set java version; TODO detect test module name
        );

        LOGGER.info("Running tests in container: " + cmd.toList());
        int exit = cmd.system();
        LOGGER.info("Execution terminated with exit code " + exit);
    }
}
