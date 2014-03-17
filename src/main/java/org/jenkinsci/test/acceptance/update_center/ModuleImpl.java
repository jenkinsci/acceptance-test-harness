package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.cloudbees.sdk.maven.RepositoryService;
import com.cloudbees.sdk.maven.RepositorySystemModule;
import com.google.inject.AbstractModule;

/**
 * Hook up Aether resolver.
 *
 * To resolve components, inject {@link RepositoryService}.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ModuleImpl extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        install(new RepositorySystemModule());
    }
}
