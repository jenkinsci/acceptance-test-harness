package org.jenkinsci.test.acceptance.cucumber;

import cucumber.runtime.Env;
import cucumber.runtime.ObjectFactoryImpl;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.BetterJavaBackend;

import java.util.Collections;

/**
 * Entry point to run cucumber tests.
 *
 * There appears to be no Maven plugin based on the conversation in
 * https://groups.google.com/forum/#!topic/cukes/Gr9TVxS0fr8
 * and http://cukes.info/running.html
 *
 * Using a separate driver makes it easier to specify options to cucumber.
 *
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length==0)
            args = new String[]{"features"};

        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env("cucumber-jvm"), args);

        ClassLoader classLoader = Main.class.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader,
                Collections.singletonList(new BetterJavaBackend(
                        new ObjectFactoryImpl(),
                        classLoader)), runtimeOptions);

        runtime.writeStepdefsJson();
        runtime.run();
        System.exit(runtime.exitStatus());
    }
}
