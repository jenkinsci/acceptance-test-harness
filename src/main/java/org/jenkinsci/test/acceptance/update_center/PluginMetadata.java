package org.jenkinsci.test.acceptance.update_center;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.VersionNumber;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;

/**
 * Databinding for installable plugin in UC.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    private final String name;
    private final String version;
    final String gav;
    private final String requiredCore;
    private final List<Dependency> dependencies;

    @JsonCreator
    public PluginMetadata(
            @JsonProperty("name") String name,
            @JsonProperty("gav") String gav,
            @JsonProperty("version") String version,
            @JsonProperty("requiredCore") String requiredCore,
            @JsonProperty("dependencies") List<Dependency> dependencies
    ) {
        this.name = name;
        this.gav = gav;
        this.version = version;
        this.requiredCore = requiredCore;
        this.dependencies = dependencies;
    }

    void init(UpdateCenterMetadata parent) {
        for (Dependency d : dependencies) {
            d.init(parent);
        }
    }

    /**
     * Calls {@link PluginManager#installPlugin(File)}.
     * @deprecated Not used when running {@link MockUpdateCenter}.
     */
    @Deprecated
    public void uploadTo(Jenkins jenkins, Injector i, String version) throws ArtifactResolutionException, IOException {
        File f = resolve(i, version);
        jenkins.getPluginManager().installPlugin(f);
    }

    public File resolve(Injector i, String version) {
        DefaultArtifact artifact = getDefaultArtifact();
        if (version != null) {
            artifact.setVersion(version);
        }

        ArtifactResolverUtil resolverUtil = i.getInstance(ArtifactResolverUtil.class);
        ArtifactResult r = resolverUtil.resolve(artifact);
        return r.getArtifact().getFile();
    }

    public DefaultArtifact getDefaultArtifact() {
        String[] t = gav.split(":");
        String gavVersion;
        if (getVersion() == null) {
            gavVersion = t[2];
        } else {
            gavVersion = getVersion();
        }
        return new DefaultArtifact(t[0], t[1], "hpi", gavVersion);
    }

    public VersionNumber requiredCore() {
        return new VersionNumber(requiredCore);
    }

    public PluginMetadata withVersion(@NonNull String v) {
        if (v == null) throw new IllegalArgumentException();
        String newGav = gav.replaceAll("\\b" + Pattern.quote(version) + "\\b", v);
        return new VersionOverride(name, newGav, v, requiredCore, dependencies);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + "," + getVersion() + "]";
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public List<Dependency> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public static final class VersionOverride extends ModifyingMetadata {

        public VersionOverride(String name, String gav, String version, String requiredCore, List<Dependency> dependencies) {
            super(name, gav, version, requiredCore, dependencies);
        }
    }

    /**
     * Use local file instead of what is configured.
     *
     * @author ogondza
     */
    public static final class LocalOverride extends ModifyingMetadata {
        private @NonNull File override;

        /**
         * Extract plugin metadata from a jpi/hpi file.
         */
        public static LocalOverride create(@NonNull File jpi) {

            List<Dependency> dependencies = new ArrayList<>();
            try (JarFile j = new JarFile(jpi)) {
                Attributes main = j.getManifest().getMainAttributes();
                String name = main.getValue("Short-Name");
                String fullVersion = main.getValue("Plugin-Version");
                if (fullVersion == null) {
                    System.err.println("Warning: no Plugin-Version found in " + jpi);
                    fullVersion = "0";
                }
                String version = trimVersion(fullVersion);
                String requiredCore = main.getValue("Jenkins-Version");
                String gav = main.getValue("Group-Id")+":"+name+":"+version;
                String dep = main.getValue("Plugin-Dependencies");
                if (dep!=null) {
                    for (String token : dep.split(",")) {
                        try {
                            dependencies.add(new Dependency(token));
                        } catch (IllegalArgumentException ex) {
                            System.err.println(ex.getMessage() + " from '" + dep + "' in " + jpi);
                        }
                    }
                }

                return new LocalOverride(name, gav, version, requiredCore, dependencies, jpi);
            } catch (IOException e) {
                throw new AssertionError("Failed to parse metadata of "+jpi,e);
            }
        }

        public LocalOverride(
                String name, String gav, String version, String requiredCore, List<Dependency> dependencies, File override
        ) {
            super(name, gav, version, requiredCore, dependencies);
            this.override = override;
        }

        /**
         * Snapshot builds often look like "Plugin-Version: 1.0-SNAPSHOT (private-08/21/2014 15:21-kohsuke)"
         * so trim off the build ID portion and just get "1.0-SNAPSHOT"
         */
        private static String trimVersion(@NonNull String version) {
            int idx = version.indexOf(" ");
            return idx > 0
                ? version.substring(0, idx)
                : version
            ;
        }

        @Override
        public File resolve(Injector i, String version) {
            return override;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + getName() + "," + override.getAbsolutePath() + "]";
        }
    }

    /**
     * Metadata that alters what is coming from the update center. All modifications *must* use a subclass of this.
     */
    public static abstract class ModifyingMetadata extends PluginMetadata {

        public ModifyingMetadata(String name, String gav, String version, String requiredCore, List<Dependency> dependencies) {
            super(name, gav, version, requiredCore, dependencies);
        }

        public final String getSha512Checksum(Injector injector) throws NoSuchAlgorithmException, IOException {
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            Path overrideFile = resolve(injector, getVersion()).toPath();
            try (DigestOutputStream dos512 = new DigestOutputStream(OutputStream.nullOutputStream(), sha512)) {
                Files.copy(overrideFile, dos512);
            }
            return Base64.getEncoder().encodeToString(sha512.digest());
        }
    }
}
