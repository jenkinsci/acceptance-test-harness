package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.io.File;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.jenkinsci.test.acceptance.utils.MavenLocalRepository;
import org.w3c.dom.NodeList;

/**
 * Allow local plugins specified via environment variables to override plugin metadata from update center.
 * <p>
 * This is to support testing of locally built plugin
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class LocalOverrideUpdateCenterMetadataDecoratorImpl implements UpdateCenterMetadataDecorator {
    @Override
    public void decorate(UpdateCenterMetadata ucm) {
        if ("true".equals(System.getenv("LOCAL_SNAPSHOTS"))) {
            File userHome = new File(System.getProperty("user.home"));
            File localRepo = MavenLocalRepository.getMavenLocalRepository();
            VersionScheme versionScheme = new GenericVersionScheme();
            for (Map.Entry<String, PluginMetadata> entry : ucm.plugins.entrySet()) {
                DefaultArtifact artifact = entry.getValue().getDefaultArtifact();
                File artifactDir = new File(
                        new File(localRepo, artifact.getGroupId().replace('.', File.separatorChar)),
                        artifact.getArtifactId());
                File metadata = new File(artifactDir, "maven-metadata-local.xml");
                if (metadata.isFile()) {
                    try {
                        Version ucVersion = versionScheme.parseVersion(artifact.getVersion());
                        NodeList versions = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder()
                                .parse(metadata)
                                .getElementsByTagName("version");
                        for (int i = 0; i < versions.getLength(); i++) {
                            String version = versions.item(i).getTextContent();
                            if (version.endsWith("-SNAPSHOT")
                                    && versionScheme.parseVersion(version).compareTo(ucVersion) > 0) {
                                File hpi = new File(
                                        new File(artifactDir, version),
                                        artifact.getArtifactId() + "-" + version + ".hpi");
                                if (hpi.isFile()) {
                                    String name = entry.getKey();
                                    System.err.println(
                                            "Overriding " + name + " " + ucVersion + " with local build of " + version);
                                    PluginMetadata m = PluginMetadata.LocalOverride.create(hpi);
                                    String parsedName = m.getName();
                                    if (!name.equals(parsedName)) {
                                        throw new AssertionError("wrong name: " + parsedName + " vs. " + name);
                                    }
                                    entry.setValue(m);
                                }
                            }
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        }

        // deprecated mechanism, as of 1.57
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            String key = e.getKey();
            if (!isPluginEnvironmentVariable(key)) {
                continue;
            }

            try {
                override(ucm, e.getValue());
                System.err.println("Using XXX.jpi/XXX_JPI env vars is deprecated. Use LOCAL_JARS instead.");
            } catch (Exception x) {
                throw new IllegalArgumentException("Unable to honor environment variable " + key, x);
            }
        }

        // past 1.57, preferred way
        String localJars = System.getenv("LOCAL_JARS");
        if (localJars != null) {
            for (String jar : localJars.split(File.pathSeparator)) {
                try {
                    override(ucm, jar);
                } catch (Exception x) {
                    throw new IllegalArgumentException("Unable to honor LOCAL_JARS environment variable", x);
                }
            }
        }
    }

    private void override(UpdateCenterMetadata ucm, String jpi) {
        File file = new File(jpi);
        if (!file.exists()) {
            throw new IllegalArgumentException("Plugin file does not exist: " + file.getAbsolutePath());
        }

        PluginMetadata m = PluginMetadata.LocalOverride.create(file);
        PluginMetadata stock = ucm.plugins.get(m.getName());
        if (stock == null) {
            System.err.println("Creating new plugin " + m.getName() + " with local build of " + m.getVersion());
        } else {
            System.err.println(
                    "Overriding " + m.getName() + " " + stock.getVersion() + " with local build of " + m.getVersion());
        }
        ucm.plugins.put(m.getName(), m);
    }

    /**
     * Returns true if the given environment variable name is an override to point to a local JPI file.
     */
    private boolean isPluginEnvironmentVariable(String name) {
        if (name.endsWith(".jpi")) {
            return true;
        }
        if (name.endsWith("_JPI")) { // http://stackoverflow.com/a/36992531/12916
            return true;
        }
        return false;
    }
}
