package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.Extension;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Configured in the wiring script via Guice by test runner to test locally built plugins.
 *
 * TODO: known problem. if plugin X depends on Y, Y is overriden, and a test case requests the presence
 * of plugin X, then {@link @org.jenkinsci.test.acceptance.junit.WithPlugins} is not smart enouguh
 * to replace X that gets automatically installed when Y is installed.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class LocalPluginOverride {
    private final Map<String,File> localPlugins = new HashMap<>();

    /**
     * Insteaed of installing the plugin from update center, use the specified local version.
     */
    public LocalPluginOverride add(File hpi) throws IOException {
        try (JarFile jar = new JarFile(hpi)) {
            Manifest m = jar.getManifest();
            String name = m.getMainAttributes().getValue("Short-Name");
            localPlugins.put(name, hpi);
        }
        return this;
    }

    public @CheckForNull File get(String shortName) {
        File f = localPlugins.get(shortName);
        if (f==null) {
            // allow  the environment variable to override a plugin
            String env = System.getenv((shortName + ".jpi").toUpperCase(Locale.ENGLISH));
            if (env!=null)
                return new File(env);
        }
        return f;
    }
}
