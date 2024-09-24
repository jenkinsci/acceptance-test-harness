package org.jenkinsci.test.acceptance.utils.aether;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Provides;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.SisuExtensions;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.jenkinsci.test.acceptance.utils.MavenLocalRepository;

/**
 * Hook up Aether resolver.
 * <p>
 * To resolve components, inject {@link RepositorySystem} and {@link RepositorySystemSession}.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class AetherModule extends AbstractModule implements ExtensionModule {

    private RepositorySystem repositorySystem;

    @Override
    protected void configure() {
        ClassSpace space = new URLClassSpace(RepositorySystem.class.getClassLoader());
        BeanLocator beanLocator = Guice.createInjector(new WireModule(new SpaceModule(space, BeanScanning.INDEX, false))
                        .with(SisuExtensions.local(space)))
                .getInstance(MutableBeanLocator.class);
        repositorySystem = beanLocator
                .locate(Key.get(RepositorySystem.class))
                .iterator()
                .next()
                .getValue();
    }

    @Provides
    public RepositorySystem newRepositorySystem() {
        return repositorySystem;
    }

    @Provides
    public RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(MavenLocalRepository.getMavenLocalRepository());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(new ConsoleTransferListener() {
            @Override
            public void transferProgressed(TransferEvent event) {
                // NOOP
            }
        });
        //        session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }
}
