package org.jenkinsci.test.acceptance.utils.aether;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.apache.maven.repository.internal.MavenAetherModule;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jenkinsci.test.acceptance.utils.MavenLocalRepository;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Hook up Aether resolver.
 *
 * To resolve components, inject {@link RepositorySystem} and {@link RepositorySystemSession}.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class AetherModule extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        install(new MavenAetherModule());
        // alternatively, use the Guice Multibindings extensions
        bind(RepositoryConnectorFactory.class).annotatedWith(Names.named("basic")).to(BasicRepositoryConnectorFactory.class);
        bind(TransporterFactory.class).annotatedWith(Names.named("file")).to(FileTransporterFactory.class);
        bind(TransporterFactory.class).annotatedWith(Names.named("http")).to(HttpTransporterFactory.class);
    }

    @Provides
    public RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( MavenLocalRepository.getMavenLocalRepository());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(new ConsoleTransferListener() {
            @Override public void transferProgressed(TransferEvent event) {
                // NOOP
            }
        });
//        session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    @Provides
    @Singleton
    Set<RepositoryConnectorFactory> provideRepositoryConnectorFactories(@Named("basic") RepositoryConnectorFactory basic) {
        Set<RepositoryConnectorFactory> factories = new HashSet<RepositoryConnectorFactory>();
        factories.add(basic);
        return Collections.unmodifiableSet(factories);
    }

    @Provides
    @Singleton
    Set<TransporterFactory> provideTransporterFactories(@Named("file") TransporterFactory file,
                                                        @Named("http") TransporterFactory http) {
        Set<TransporterFactory> factories = new HashSet<TransporterFactory>();
        factories.add(file);
        factories.add(http);
        return Collections.unmodifiableSet(factories);
    }
}
