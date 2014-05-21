package org.jenkinsci.test.acceptance.update_center;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.ProvidedBy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
     * Find all the transitive dependency plugins of the given plugins, in the order of installation
     */
    public List<PluginMetadata> transitiveDependenciesOf(Collection<String> names) {
        List<PluginMetadata> r = new ArrayList<>();
        for (String n : names)
            transitiveDependenciesOf(n, r);
        return r;
    }

    private void transitiveDependenciesOf(String n, List<PluginMetadata> r) {
        PluginMetadata p = plugins.get(n);
        if (p==null)    return;

        for (Dependency d : p.dependencies)
            if (!d.optional)
                transitiveDependenciesOf(d.name, r);

        if (!r.contains(p))
            r.add(p);
    }
}
