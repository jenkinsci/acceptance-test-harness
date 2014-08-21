package org.jenkinsci.test.acceptance.update_center;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * Allow local plugins specified via environment variables to override plugin metadata from update center.
 *
 * This is to support testing of locally built plugin
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalOverrideUpdateCenterMetadataDecoratorImpl implements UpdateCenterMetadataDecorator {
    @Override
    public void decorate(UpdateCenterMetadata ucm) {
        for (Map.Entry<String,String> e : System.getenv().entrySet()) {
            if (e.getKey().endsWith(".jpi")) {
                String shortName = e.getKey().substring(e.getKey().length()-4);
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
