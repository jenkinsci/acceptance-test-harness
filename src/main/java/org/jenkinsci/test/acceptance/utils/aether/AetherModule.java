package org.jenkinsci.test.acceptance.utils.aether;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultModelCacheFactory;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.repository.internal.ModelCacheFactory;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.ChecksumExtractor;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jenkinsci.test.acceptance.utils.MavenLocalRepository;

/**
 * Hook up Aether resolver.
 * <p>
 * To resolve components, inject {@link RepositorySystem} and {@link RepositorySystemSession}.
 * <p>
 * Here, we assemble a complete module by using {@link org.eclipse.aether.impl.guice.AetherModule} (see its Javadoc)
 * and adding bits from Maven itself (binding those components that complete the repository system).
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class AetherModule extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        // NOTE: see org.eclipse.aether.impl.guice.AetherModule Javadoc:
        // org.eclipse.aether.impl.guice.AetherModule alone is "ready-made" but incomplete.
        // To have a complete resolver, we actually need to bind the missing components making the module complete.
        install(new org.eclipse.aether.impl.guice.AetherModule());

        // make module "complete" by binding things not bound by org.eclipse.aether.impl.guice.AetherModule
        bind(ArtifactDescriptorReader.class)
                .to(DefaultArtifactDescriptorReader.class)
                .in(Singleton.class);
        bind(VersionResolver.class).to(DefaultVersionResolver.class).in(Singleton.class);
        bind(VersionRangeResolver.class).to(DefaultVersionRangeResolver.class).in(Singleton.class);
        bind(MetadataGeneratorFactory.class)
                .annotatedWith(Names.named("snapshot"))
                .to(SnapshotMetadataGeneratorFactory.class)
                .in(Singleton.class);

        bind(MetadataGeneratorFactory.class)
                .annotatedWith(Names.named("versions"))
                .to(VersionsMetadataGeneratorFactory.class)
                .in(Singleton.class);

        bind(RepositoryConnectorFactory.class)
                .annotatedWith(Names.named("basic"))
                .to(BasicRepositoryConnectorFactory.class);
        bind(TransporterFactory.class).annotatedWith(Names.named("file")).to(FileTransporterFactory.class);
        bind(TransporterFactory.class).annotatedWith(Names.named("http")).to(HttpTransporterFactory.class);
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

    /**
     * Checksum extractors (none).
     */
    @Provides
    @Singleton
    Map<String, ChecksumExtractor> provideChecksumExtractors() {
        return Collections.emptyMap();
    }

    /**
     * Repository system connectors (needed for remote transport).
     */
    @Provides
    @Singleton
    Set<RepositoryConnectorFactory> provideRepositoryConnectorFactories(
            @Named("basic") RepositoryConnectorFactory basic) {
        Set<RepositoryConnectorFactory> factories = new HashSet<>();
        factories.add(basic);
        return Collections.unmodifiableSet(factories);
    }

    /**
     * Repository system transporters (needed for remote transport).
     */
    @Provides
    @Singleton
    Set<TransporterFactory> provideTransporterFactories(
            @Named("file") TransporterFactory file, @Named("http") TransporterFactory http) {
        Set<TransporterFactory> factories = new HashSet<>();
        factories.add(file);
        factories.add(http);
        return Collections.unmodifiableSet(factories);
    }

    /**
     * Repository metadata generators (needed for remote transport).
     */
    @Provides
    @Singleton
    Set<MetadataGeneratorFactory> provideMetadataGeneratorFactories(
            @Named("snapshot") MetadataGeneratorFactory snapshot,
            @Named("versions") MetadataGeneratorFactory versions) {
        Set<MetadataGeneratorFactory> factories = new HashSet<>(2);
        factories.add(snapshot);
        factories.add(versions);
        return Collections.unmodifiableSet(factories);
    }

    /**
     * Simple instance provider for model builder factory.
     */
    @Provides
    ModelBuilder provideModelBuilder() {
        return new DefaultModelBuilderFactory().newInstance();
    }

    @Provides
    ModelCacheFactory provideModelCacheFactory() {
        return new DefaultModelCacheFactory();
    }
}
