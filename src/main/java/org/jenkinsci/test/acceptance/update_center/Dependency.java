package org.jenkinsci.test.acceptance.update_center;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Databinding for dependency of a plugin.
 *
 * @author Kohsuke Kawaguchi
 */
public class Dependency {
    private static final String OPTIONAL = ";resolution:=optional";

    public final String version;
    public final String name;
    public final boolean optional;

    private UpdateCenterMetadata owner;

    public Dependency(String specification) {
        optional = specification.endsWith(OPTIONAL);
        if (optional) {
            specification = specification.substring(0, specification.length() - OPTIONAL.length());
        }
        String[] tokens = specification.split(":");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Unable to parse dependency declaration " + specification);
        }
        name = tokens[0];
        version = tokens[1];
    }

    public Dependency(
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("optional") boolean optional) {
        this.name = name;
        this.version = version;
        this.optional = optional;
    }

    void init(UpdateCenterMetadata owner) {
        this.owner = owner;
    }

    public PluginMetadata get() {
        return owner.plugins.get(name);
    }

    @Override
    public String toString() {
        return "Dependency[" + name + (version == null ? "" : ("@" + version)) + ";optional=" + optional + "]";
    }
}
