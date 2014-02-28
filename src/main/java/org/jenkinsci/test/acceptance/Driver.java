package org.jenkinsci.test.acceptance;

import cucumber.runtime.Env;
import cucumber.runtime.Glue;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;

import java.util.Collections;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class Driver {
    public static void main(String[] args) throws Exception {
        args = new String[]{"features/test.feature"};
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env("cucumber-jvm"), args);

        ClassLoader classLoader = Driver.class.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
//        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader,
                Collections.singletonList(new JavaBackend(resourceLoader) {
                    @Override
                    public void loadGlue(Glue glue, List<String> gluePaths) {
                        super.loadGlue(glue, gluePaths);
                    }
                }), runtimeOptions);
        runtime.writeStepdefsJson();
        runtime.run();
        System.exit(runtime.exitStatus());
    }
}
