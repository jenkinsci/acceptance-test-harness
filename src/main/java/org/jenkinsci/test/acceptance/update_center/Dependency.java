package org.jenkinsci.test.acceptance.update_center;

/**
 * Databinding for dependency of a plugin.
 *
 * @author Kohsuke Kawaguchi
 */
public class Dependency {
    public String version;
    public String name;
    public boolean optional;

    private UpdateCenterMetadata owner;

    void init(UpdateCenterMetadata owner) {
        this.owner = owner;
    }

    public PluginMetadata get() {
        return owner.plugins.get(name);
    }
}
