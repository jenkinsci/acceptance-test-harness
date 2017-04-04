package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.w3c.dom.NodeList;

/**
 * Allow local plugins specified via environment variables to override plugin metadata from update center.
 *
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
            File localRepo = new File(new File(userHome, ".m2"), "repository");
            VersionScheme versionScheme = new GenericVersionScheme();
            for (Iterator<Map.Entry<String, PluginMetadata>> it = ucm.plugins.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String,PluginMetadata> entry = it.next();
                DefaultArtifact artifact = entry.getValue().getDefaultArtifact();
                File artifactDir = new File(new File(localRepo, artifact.getGroupId().replace('.', File.separatorChar)), artifact.getArtifactId());
                File metadata = new File(artifactDir, "maven-metadata-local.xml");
                if (metadata.isFile()) {
                    try {
                        Version ucVersion = versionScheme.parseVersion(artifact.getVersion());
                        NodeList versions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(metadata).getElementsByTagName("version");
                        for (int i = 0; i < versions.getLength(); i++) {
                            String version = versions.item(i).getTextContent();
                            if (version.endsWith("-SNAPSHOT") && versionScheme.parseVersion(version).compareTo(ucVersion) > 0) {
                                File hpi = new File(new File(artifactDir, version), artifact.getArtifactId() + "-" + version + ".hpi");
                                if (hpi.isFile()) {
                                    String name = entry.getKey();
                                    System.err.println("Overriding " + name + " " + ucVersion + " with local build of " + version);
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
        Map<String,File> overrides = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            String name = e.getKey();
            if (name.endsWith(".jpi")) {
                name = name.replace(".jpi", "");
                overrides.put(name, new File(e.getValue()));
            }
        }
        String localPlugins = System.getenv("FORCE_PLUGIN_FILES");
        if (StringUtils.isNotBlank(localPlugins)) {
            File file = new File(localPlugins);
            if (!file.isFile()) {
                throw new AssertionError("Environment variable 'FORCE_PLUGIN_FILES' specifies a non-existent file: " + file);
            }
            Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(file.toPath())) {
                properties.load(is);
            } catch (IOException e) {
                throw new AssertionError("Could not read 'FORCE_PLUGIN_FILES' file: " + file, e);
            }
            for (Map.Entry<Object,Object> e: properties.entrySet()) {
                overrides.put((String)e.getKey(), new File((String)e.getValue()));
            }
        }
        for (Map.Entry<String, File> e : overrides.entrySet()) {
            if (ucm.plugins.get(e.getKey()) == null) {
                throw new IllegalArgumentException("Plugin does not exists in update center: " + e.getKey());
            }
            if (!e.getValue().isFile()) {
                throw new IllegalArgumentException(
                        "Plugin file for " + e.getKey() + " does not exist: " + e.getValue().getAbsolutePath());
            }
            PluginMetadata m = PluginMetadata.LocalOverride.create(e.getValue());
            ucm.plugins.put(m.getName(), m);
        }
    }
}
