package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.io.File;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.w3c.dom.Element;
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
            for (Map.Entry<String,PluginMetadata> entry : ucm.plugins.entrySet()) {
                DefaultArtifact artifact = entry.getValue().getDefaultArtifact();
                File artifactDir = new File(new File(localRepo, artifact.getGroupId().replace('.', File.separatorChar)), artifact.getArtifactId());
                File metadata = new File(artifactDir, "maven-metadata-local.xml");
                if (metadata.isFile()) {
                    try {
                        Version ucVersion = versionScheme.parseVersion(artifact.getVersion());
                        NodeList versions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(metadata).getElementsByTagName("version");
                        for (int i = 0; i < versions.getLength(); i++) {
                            String version = ((Element) versions.item(i)).getTextContent();
                            if (version.endsWith("-SNAPSHOT") && versionScheme.parseVersion(version).compareTo(ucVersion) > 0) {
                                File hpi = new File(new File(artifactDir, version), artifact.getArtifactId() + "-" + version + ".hpi");
                                if (hpi.isFile()) {
                                    System.err.println("Overriding " + entry.getKey() + " " + ucVersion + " with local build of " + version);
                                    PluginMetadata m = PluginMetadata.LocalOverride.create(hpi);
                                    ucm.plugins.put(m.getName(), m);
                                }
                            }
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        }
        for (Map.Entry<String,String> e : System.getenv().entrySet()) {
            if (e.getKey().endsWith(".jpi")) {
                PluginMetadata m = PluginMetadata.LocalOverride.create(new File(e.getValue()));
                ucm.plugins.put(m.getName(), m);
            }
        }
    }
}
