package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.util.Map;

/**
 * Download particular plugin version based on environement variable.
 */
@Extension
public class DownloadOverrideUpdateCenterMetadataDecorator implements UpdateCenterMetadataDecorator {
    private static final String VARNAME = "VERSION_OVERRIDES";

    @Override
    public void decorate(UpdateCenterMetadata ucm) {
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            String key = e.getKey();
            if (key.endsWith(".version")) {
                // TODO keep this in for a while and remove it
                System.err.println("Using XXX.version (" + key
                        + ") env vars for plugin version override is no longer supported. Use " + VARNAME
                        + " instead.");
            }
        }

        String overrides = System.getenv(VARNAME);
        if (overrides != null) {
            for (String override : overrides.split(",")) {
                String[] chunks = override.split("=");
                if (chunks.length != 2) {
                    throw new Error("Unable to parse " + override + " as a " + VARNAME);
                }
                override(ucm, chunks[0], chunks[1]);
            }
        }
    }

    private void override(UpdateCenterMetadata ucm, String name, String version) {
        PluginMetadata original = ucm.plugins.get(name);
        if (original == null) {
            throw new IllegalArgumentException("Plugin does not exists in update center: " + name);
        }
        ucm.plugins.put(name, original.withVersion(version));
        System.err.println("Overriding the version of " + name + " with " + version);
    }
}
