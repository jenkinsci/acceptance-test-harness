package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.Extension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;

/**
 * Download particular plugin version based on environement variable.
 */
@Extension
public class DownloadOverrideUpdateCenterMetadataDecorator implements UpdateCenterMetadataDecorator {

    @Override
    public void decorate(UpdateCenterMetadata ucm) {
        Map<String,String> overrides = new LinkedHashMap<>();
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
        String localPlugins = System.getenv("FORCE_PLUGIN_VERSIONS");
        if (StringUtils.isNotBlank(localPlugins)) {
            File file = new File(localPlugins);
            if (!file.isFile()) {
                throw new AssertionError(
                        "Environment variable 'FORCE_PLUGIN_VERSIONS' specifies a non-existent file: " + file);
            }
            Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(file.toPath())) {
                properties.load(is);
            } catch (IOException e) {
                throw new AssertionError("Could not read 'FORCE_PLUGIN_VERSIONS' file: " + file, e);
            }
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                overrides.put((String) e.getKey(), (String) e.getValue());
            }
        }
        for (Map.Entry<String, String> e : overrides.entrySet()) {
            PluginMetadata original = ucm.plugins.get(e.getKey());
            if (original == null) {
                throw new IllegalArgumentException("Plugin does not exists in update center: " + e.getKey());
            }
            ucm.plugins.put(e.getKey(), original.withVersion(e.getValue()));
        }
    }
}
