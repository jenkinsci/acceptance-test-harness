package org.jenkinsci.test.acceptance;

import com.google.inject.AbstractModule;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.jenkinsci.groovy.binder.GroovyWiringModule;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.AdditionalBinderDsl;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.openqa.selenium.WebDriver;

/**
 * Integrates user-configuration (such as which {@link WebDriver} and {@link JenkinsController}
 * to use) into Guice.
 * <p>
 * We expect the configuration script to be specified in Groovy
 * (see <a href="https://github.com/jenkinsci/lib-groovy-guice-binder">lib-groovy-guice-binder</a>)
 * and its location be given via "CONFIG" system property or environment variable.
 *
 * @author Kohsuke Kawaguchi
 */
public class Config extends AbstractModule {
    @Override
    protected void configure() {
        try {
            String loc = System.getProperty("CONFIG");
            if (loc==null)
                loc = System.getenv("CONFIG");
            if (loc==null) {
                // none specified.
                return;
            }

            GroovyWiringModule m;

            File f = new File(loc);
            if (f.exists()) {
                m = GroovyWiringModule.allOf(f);
            } else {
                m = new GroovyWiringModule(new URL(loc));
            }

            m.getCompilerConfiguration().setScriptBaseClass(AdditionalBinderDsl.class.getName());
            m.addStarImports(
                JenkinsController.class.getPackage().getName(),
                WebDriver.class.getPackage().getName()
            );
            m.addImports(TestScope.class);

            // install the config
            install(m);
        } catch (IOException e) {
            throw new Error("Failed to load configuration script",e);
        }
    }
}
