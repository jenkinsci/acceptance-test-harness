package org.jenkinsci.test.acceptance;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import cucumber.runtime.Env;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.BetterJavaBackend;
import cucumber.runtime.ObjectFactoryImpl;

import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class Driver {
    public static void main(String[] args) throws Exception {
        if (args.length==0)
            args = new String[]{"features"};

        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env("cucumber-jvm"), args);

        ClassLoader classLoader = Driver.class.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader,
                Collections.singletonList(new BetterJavaBackend(
                        new ObjectFactoryImpl(new ExtensionFinder(classLoader)),
                        classLoader)), runtimeOptions);

        runtime.writeStepdefsJson();
        runtime.run();
        System.exit(runtime.exitStatus());
    }
}
