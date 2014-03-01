package org.jenkinsci.test.acceptance;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import org.jenkinsci.groovy.binder.GroovyWiringModule;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Integrates user-configuration (such as which {@link WebDriver} and {@link JenkinsController}
 * to use) into Guice.
 *
 * We expect the configuration script to be specified in Groovy (see https://github.com/jenkinsci/lib-groovy-guice-binder)
 * and its location be given via "CONFIG" system property or environment variable.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class Config extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        try {
            FallbackConfig base = new FallbackConfig();

            String loc = System.getProperty("CONFIG");
            if (loc==null)
                loc = System.getenv("CONFIG");
            if (loc==null) {
                // none specified. fallback.
                base.configure(binder());
                return;
            }

            GroovyWiringModule m;

            File f = new File(loc);
            if (f.exists()) {
                m = GroovyWiringModule.allOf(f);
            } else {
                m = new GroovyWiringModule(new URL(loc));
            }

            // install the config
            Modules.override(base).with(m).configure(binder());
        } catch (IOException e) {
            throw new Error("Failed to load configuration script",e);
        }
    }
}
