package org.jenkinsci.test.acceptance;

import cucumber.runtime.Env;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend2;

import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class Driver {
    public static void main(String[] args) throws Exception {
        args = new String[]{"features/test.feature"};
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env("cucumber-jvm"), args);

        final ClassLoader classLoader = Driver.class.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader,
                Collections.singletonList(new JavaBackend2(resourceLoader,classLoader)), runtimeOptions);
        runtime.writeStepdefsJson();
        runtime.run();
        System.exit(runtime.exitStatus());
    }
}
