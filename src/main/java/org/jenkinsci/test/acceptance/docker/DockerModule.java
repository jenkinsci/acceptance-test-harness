package org.jenkinsci.test.acceptance.docker;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DockerModule extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        requestStaticInjection(Docker.class);
    }
}
