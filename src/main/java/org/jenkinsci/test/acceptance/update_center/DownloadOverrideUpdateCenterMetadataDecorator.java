package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.util.Map;
import javax.inject.Inject;

import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;

/**
 * Download particular plugin version based on environement variable.
 */
@Extension
public class DownloadOverrideUpdateCenterMetadataDecorator implements UpdateCenterMetadataDecorator {

    @Override
    public void decorate(UpdateCenterMetadata ucm) {
        for (Map.Entry<String,String> e : System.getenv().entrySet()) {
            String key = e.getKey();
            if (key.endsWith(".version")) {
                String name = key.substring(0, key.length() - 8);
                String version = e.getValue();

                PluginMetadata original = ucm.plugins.get(name);
                if (original == null) throw new IllegalArgumentException("Plugin does not exists in update center: " + name);
                ucm.plugins.put(name, original.withVersion(version));
            }
        }
    }
}
