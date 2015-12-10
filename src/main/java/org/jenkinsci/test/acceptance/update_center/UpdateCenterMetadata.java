package org.jenkinsci.test.acceptance.update_center;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.ProvidedBy;

import hudson.util.VersionNumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Databinding for Update Center metadata
 *
 * @author Kohsuke Kawaguchi
 */
@ProvidedBy(CachedUpdateCenterMetadataLoader.class)
public class UpdateCenterMetadata {
    /**
     * Details of plugins by {@linkplain PluginMetadata#name their name}.
     */
    public final Map<String,PluginMetadata> plugins = new HashMap<>();

    public String id;

    /**
     *
     * @param data
     *      .json or .json.html file served from update center.
     */
    public static UpdateCenterMetadata parse(File data) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(data));
        r.readLine();   // the first line is preamble
        String json = r.readLine(); // the 2nd line is the actual JSON
        r.readLine();   // the third line is postamble

        ObjectMapper om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        UpdateCenterMetadata v = om.readValue(json, UpdateCenterMetadata.class);
        v.init();
        return v;
    }

    private void init() {
        for (PluginMetadata pm : plugins.values()) {
            pm.init(this);
        }
    }

    /**
     * Find all the transitive dependency plugins of the given plugins, in the order of installation.
     *
     * Resolved plugins set should satisfy required versions including Jenkins version.
     *
     * @throws UnableToResolveDependencies When there requested plugin version can not be installed.
     */
    public List<PluginMetadata> transitiveDependenciesOf(VersionNumber jenkins, Map<String, String> names) throws UnableToResolveDependencies {
        List<PluginMetadata> set = new ArrayList<>();
        for (Map.Entry<String, String> n : names.entrySet()) {
            PluginMetadata p = plugins.get(n.getKey());
            if (p==null) throw new IllegalArgumentException("No such plugin " + n.getKey());
            if (p.requiredCore().isNewerThan(jenkins)) {
                throw new UnableToResolveDependencies(String.format(
                        "Unable to install %s plugin because of core dependency. Requeried: %s Used: %s",
                        p, p.requiredCore(), jenkins
                ));
            }

            transitiveDependenciesOf(jenkins, p, n.getValue(), set);
        }
        return set;
    }

    private void transitiveDependenciesOf(VersionNumber jenkins, PluginMetadata p, String v, List<PluginMetadata> set) {
        for (Dependency d : p.getDependencies()) {
            if (d.optional) continue;
            transitiveDependenciesOf(jenkins, plugins.get(d.name), d.version, set);
        }

        if (!set.contains(p)) {
            PluginMetadata use = p;
            if (use.requiredCore().isNewerThan(jenkins)) {
                // If latest version is too new for current Jenkins, use the declared one
                p.setVersion(v);
            }

            set.add(use);
        }
    }

    public static class UnableToResolveDependencies extends RuntimeException {

        public UnableToResolveDependencies(String format) {
            super(format);
        }
    }
}
