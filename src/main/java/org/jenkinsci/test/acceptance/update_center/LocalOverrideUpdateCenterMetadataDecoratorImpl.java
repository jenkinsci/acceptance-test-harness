package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilderFactory;
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
                // TODO would be more proper to go through Aether APIs to do this
                String[] gav = entry.getValue().gav.split(":");
                File artifactDir = new File(new File(localRepo, gav[0].replace('.', File.separatorChar)), gav[1]);
                File metadata = new File(artifactDir, "maven-metadata-local.xml");
                if (metadata.isFile()) {
                    try {
                        Version ucVersion = versionScheme.parseVersion(gav[2]);
                        NodeList versions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(metadata).getElementsByTagName("version");
                        for (int i = 0; i < versions.getLength(); i++) {
                            String version = ((Element) versions.item(i)).getTextContent();
                            if (version.endsWith("-SNAPSHOT") && versionScheme.parseVersion(version).compareTo(ucVersion) > 0) {
                                File hpi = new File(new File(artifactDir, version), gav[1] + "-" + version + ".hpi");
                                if (hpi.isFile()) {
                                    System.err.println("Overriding " + entry.getKey() + " " + ucVersion + " with local build of " + version);
                                    PluginMetadata m = parseMetadata(hpi);
                                    ucm.plugins.put(m.name, m);
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
                PluginMetadata m = parseMetadata(new File(e.getValue()));
                ucm.plugins.put(m.name, m);
            }
        }
    }

    /**
     * Looks at the manifest of a plugin jpi file and parses that into {@link PluginMetadata} format.
     */
    private PluginMetadata parseMetadata(File jpi) {
        PluginMetadata m = new PluginMetadata();
        m.dependencies = new ArrayList<Dependency>();
        try (JarFile j = new JarFile(jpi)) {
            Attributes main = j.getManifest().getMainAttributes();
            m.name = main.getValue("Short-Name");
            m.version = main.getValue("Plugin-Version");
            m.gav = main.getValue("Group-Id")+":"+m.name+":"+m.version;
            m.url = jpi.toURL();
            m.override = jpi;
            String dep = main.getValue("Plugin-Dependencies");
            if (dep!=null) {
                for (String token : dep.split(",")) {
                    Dependency d = new Dependency();
                    d.optional = dep.endsWith(OPTIONAL);
                    if(d.optional)
                        token = token.substring(0, token.length()-OPTIONAL.length());
                    String[] tokens = token.split(":");
                    d.name = tokens[0];
                    d.version = tokens[1];
                    m.dependencies.add(d);
                }
            }
            tidyUpVersion(m);
        } catch (IOException e) {
            throw new AssertionError("Failed to parse metadata of "+jpi,e);
        }
        return m;
    }

    /**
     * Snapshot builds often look like "Plugin-Version: 1.0-SNAPSHOT (private-08/21/2014 15:21-kohsuke)"
     * so trim off the build ID portion and just get "1.0-SNAPSHOT"
     */
    private void tidyUpVersion(PluginMetadata m) {
        int idx = m.version.indexOf(" ");
        if (idx>0)
            m.version = m.version.substring(0,idx);
    }

    private static final String OPTIONAL = ";resolution:=optional";
}
