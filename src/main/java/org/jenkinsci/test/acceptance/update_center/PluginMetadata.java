package org.jenkinsci.test.acceptance.update_center;

import hudson.util.VersionNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Injector;

import static org.apache.http.entity.ContentType.*;

/**
 * Databinding for installable plugin in UC.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    private final String name;
    private final String version;
    private final String gav;
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
     * @deprecated in favour of {@link PluginManager#installPlugin(File)}.
     */
    @Deprecated
    public void uploadTo(Jenkins jenkins, Injector i, String version) throws ArtifactResolutionException, IOException {
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost post = new HttpPost(jenkins.url("pluginManager/uploadPlugin").toExternalForm());
        File f = resolve(i, version);
        HttpEntity e = MultipartEntityBuilder.create()
                .addBinaryBody("name", f, APPLICATION_OCTET_STREAM, getName() + ".jpi")
                .build();
        post.setEntity(e);

        HttpResponse response = httpclient.execute(post);
        if (response.getStatusLine().getStatusCode() >= 400) {
            throw new IOException("Failed to upload plugin: " + response.getStatusLine() + "\n" +
                    IOUtils.toString(response.getEntity().getContent()));
        } else {
            System.out.format("Plugin %s installed\n", f);
        }
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

    public PluginMetadata withVersion(@Nonnull String v) {
        if (v == null) throw new IllegalArgumentException();

        return new PluginMetadata(name, gav, v, requiredCore, dependencies);
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

    /**
     * Use local file instead of what is configured.
     *
     * @author ogondza
     */
    public static final class LocalOverride extends PluginMetadata {
        private @Nonnull File override;

        /**
         * Extract plugin metadata from a jpi/hpi file.
         */
        public static LocalOverride create(@Nonnull File jpi) {

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
        private static String trimVersion(@Nonnull String version) {
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
}
