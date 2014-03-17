package org.jenkinsci.test.acceptance.update_center;

import java.net.URL;
import java.util.List;

/**
 * Databinding for installable plugin in UC.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    public String name, version, gav;
    public List<Dependency> dependencies;
    public URL url;

    void init(UpdateCenterMetadata parent) {
        for (Dependency d : dependencies) {
            d.init(parent);
        }
    }
}
