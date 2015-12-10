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
    @Inject ArtifactResolverUtil resolver;

    @Override
    public void decorate(UpdateCenterMetadata ucm) {
        for (Map.Entry<String,String> e : System.getenv().entrySet()) {
            String key = e.getKey();
            if (key.endsWith(".version")) {
                String name = key.substring(0, key.length() - 8);
                String version = e.getValue();
                PluginMetadata plugin = ucm.plugins.get(name);
                plugin.setVersion(version);
            }
        }
    }
}
