package org.jenkinsci.test.acceptance.controller;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

public class WinstoneControllerTest {
    private Injector i;
    private File tempFile;

    @Before
    public void setUp() {
        i = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                try {
                    tempFile = File.createTempFile("junit", "test");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bind(String.class).annotatedWith(Names.named("WORKSPACE")).toInstance("/tmp/");
                bind(String.class).annotatedWith(Names.named("quite")).toInstance("");
                bind(File.class).annotatedWith(Names.named("form-element-path.hpi")).toInstance(tempFile);
                bind(File.class).annotatedWith(Names.named("jenkins.war")).toInstance(tempFile);
            }
        });
    }

    @Test
    public void constructWithJavaHome() {
        File javaHome = new File("/tmp/path/to/java_home");
        javaHome.mkdirs();
        WinstoneController winstoneController = new WinstoneController(i, javaHome);
        Assert.assertSame("Expected java home directory given to constructor", javaHome, winstoneController.getJavaHome());
    }
}
