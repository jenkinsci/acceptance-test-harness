package org.jenkinsci.test.acceptance.update_center;

import hudson.util.VersionNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import static org.apache.http.entity.ContentType.*;

/**
 * Databinding for installable plugin in UC.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginMetadata.class);
    public String name, version, gav;
    public String requiredCore;
    public List<Dependency> dependencies;

    /**
     * If non-null, use this file instead of the one pointed by {@link #gav}.
     */
    public File override;

    void init(UpdateCenterMetadata parent) {
        for (Dependency d : dependencies) {
            d.init(parent);
        }
    }

    public PluginMetadata() {}

    /**
     * Extract plugin metadata from a jpi/hpi file.
     */
    public PluginMetadata(@Nonnull File jpi) {
        final String OPTIONAL = ";resolution:=optional";

        dependencies = new ArrayList<Dependency>();
        try (JarFile j = new JarFile(jpi)) {
            Attributes main = j.getManifest().getMainAttributes();
            name = main.getValue("Short-Name");
            version = trimVersion(main.getValue("Plugin-Version"));
            requiredCore = main.getValue("Jenkins-Version");
            gav = main.getValue("Group-Id")+":"+name+":"+version;
            override = jpi;
            String dep = main.getValue("Plugin-Dependencies");
            if (dep!=null) {
                for (String token : dep.split(",")) {
                    Dependency d = new Dependency();
                    d.optional = token.endsWith(OPTIONAL);
                    if(d.optional)
                        token = token.substring(0, token.length()-OPTIONAL.length());
                    String[] tokens = token.split(":");
                    if (tokens.length != 2) {
                        System.err.println("Bad token ‘" + token + "’ from ‘" + dep + "’ in " + jpi);
                        continue;
                    }
                    d.name = tokens[0];
                    d.version = tokens[1];
                    dependencies.add(d);
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Failed to parse metadata of "+jpi,e);
        }
    }

    /**
     * Snapshot builds often look like "Plugin-Version: 1.0-SNAPSHOT (private-08/21/2014 15:21-kohsuke)"
     * so trim off the build ID portion and just get "1.0-SNAPSHOT"
     */
    private String trimVersion(@Nonnull String version) {
        int idx = version.indexOf(" ");
        return idx > 0
            ? version.substring(0, idx)
            : version
        ;
    }

    /**
     * @param jenkins
     * @param i
     * @param version The version of the plugin you want to upload
     * @throws ArtifactResolutionException
     * @throws IOException
     */
    public void uploadTo(Jenkins jenkins, Injector i, String version) throws ArtifactResolutionException, IOException {
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost post = new HttpPost(jenkins.url("pluginManager/uploadPlugin").toExternalForm());
        File f = resolve(i, version);
        HttpEntity e = MultipartEntityBuilder.create()
                .addBinaryBody("name", f, APPLICATION_OCTET_STREAM, name + ".jpi")
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
        if (override!=null) return override;

        RepositorySystem rs = i.getInstance(RepositorySystem.class);
        RepositorySystemSession rss = i.getInstance(RepositorySystemSession.class);

        ArtifactResolverUtil resolverUtil = new ArtifactResolverUtil(rs, rss);
        ArtifactResult r = resolverUtil.resolve(gav, version);
        return r.getArtifact().getFile();
    }

    public VersionNumber requiredCore() {
        return new VersionNumber(requiredCore);
    }

    public PluginMetadata versionOf(String v) {
        if (v == null) throw new IllegalArgumentException();

        PluginMetadata ret = new PluginMetadata();
        ret.name = name;
        ret.version = v;
        ret.gav = gav;
        ret.requiredCore = requiredCore;
        return ret;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + name + "," + version + "]";
    }
}
