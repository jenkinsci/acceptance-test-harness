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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.po.Jenkins;

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
    public Map<String,PluginMetadata> plugins = new HashMap<>();

    public String id;

    /**
     * Create metadata parsing Jenkins update center file.
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

    public static UpdateCenterMetadata get(String id, Map<String,PluginMetadata> plugins) {
        UpdateCenterMetadata ucm = new UpdateCenterMetadata();
        ucm.id = id;
        ucm.plugins.putAll(plugins);
        ucm.init();
        return ucm;
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
     * Transitive dependencies will not be included if there is an already valid version of the plugin installed.
     *
     * @throws UnableToResolveDependencies When there requested plugin version can not be installed.
     */
    public List<PluginMetadata> transitiveDependenciesOf(Jenkins jenkins, Collection<PluginSpec> plugins) throws UnableToResolveDependencies {
        return this.transitiveDependenciesOf(jenkins, plugins, false);
    }

    /**
     * Find all the transitive dependency plugins of the given plugins in the order of installation, including optional plugins that are marked as required.
     *
     * Resolved plugins set should satisfy required versions including Jenkins version.
     *
     * Transitive dependencies will not be included if there is an already valid version of the plugin installed.
     *
     * @throws UnableToResolveDependencies When there requested plugin version can not be installed.
     */
    public List<PluginMetadata> transitiveDependenciesIncludingOptionalsOf(Jenkins jenkins, Collection<PluginSpec> plugins) throws UnableToResolveDependencies {
        return this.transitiveDependenciesOf(jenkins, plugins, true);
    }

    private List<PluginMetadata> transitiveDependenciesOf(Jenkins jenkins, Collection<PluginSpec> requiredPlugins, boolean includeOptionals) throws UnableToResolveDependencies {
        List<PluginMetadata> result = new ArrayList<>();
        for (PluginSpec n : requiredPlugins) {
            PluginMetadata p = this.plugins.get(n.getName());
            if (p==null) throw new IllegalArgumentException("No such plugin " + n.getName());
            if (p.requiredCore().isNewerThan(jenkins.getVersion())) {
                throw new UnableToResolveDependencies(String.format(
                        "Unable to install %s plugin because of core dependency. Required: %s Used: %s",
                        p, p.requiredCore(), jenkins
                ));
            }

            transitiveDependenciesOf(jenkins, p, n.getVersion(), result, requiredPlugins, includeOptionals);
        }

        return result;
    }

    private void transitiveDependenciesOf(Jenkins jenkins, PluginMetadata p, String v, List<PluginMetadata> result, Collection<PluginSpec> requiredPlugins, boolean includeOptionals) {
        for (Dependency d : p.getDependencies()) {
            if (!shouldBeIncluded(jenkins, d)) continue;
            if ((d.optional && !includeOptionals) || (d.optional && includeOptionals && !optionalRequiredPlugin(d.name, requiredPlugins))) continue;

            PluginMetadata depMetaData = plugins.get(d.name);
            if (depMetaData == null) {
                throw new UnableToResolveDependencies(
                    String.format("Unable to install dependency '%s' for '%s': plugin not found", d, p)
                );
            }

            transitiveDependenciesOf(jenkins, depMetaData, d.version, result, requiredPlugins, includeOptionals);
        }

        if (!result.contains(p)) {
            PluginMetadata use = p;
            if (use.requiredCore().isNewerThan(jenkins.getVersion())) {
                // If latest version is too new for current Jenkins, use the declared one
                use = p.withVersion(v);
            }

            result.add(use);
        }
    }

    /**
     * Find all the optional plugins of the given plugins in the order of installation.
     *
     * @throws UnableToResolveDependencies When there requested plugin version can not be installed.
     */
    public Map<Integer, List<String>> optionalDependenciesOf(Jenkins jenkins, Collection<PluginSpec> requiredPlugins) throws UnableToResolveDependencies {
        Map<Integer, List<String>> optionalDependencies = new TreeMap<>(Collections.<Integer>reverseOrder());

        for (PluginSpec n : requiredPlugins) {
            PluginMetadata p = this.plugins.get(n.getName());
            if (p==null) throw new IllegalArgumentException("No such plugin " + n.getName());

            if (p.requiredCore().isNewerThan(jenkins.getVersion())) {
                throw new UnableToResolveDependencies(String.format(
                        "Unable to install %s plugin because of core dependency. Required: %s Used: %s",
                        p, p.requiredCore(), jenkins
                ));
            }

            optionalDependenciesOf(p, optionalDependencies, requiredPlugins, false, 0);
        }

        return optionalDependencies;
    }

    private void optionalDependenciesOf(PluginMetadata p, Map<Integer, List<String>> result, Collection<PluginSpec> requiredPlugins, boolean includeSelf, int depth) {
        for (Dependency d : p.getDependencies()) {
            if (d.optional && !optionalRequiredPlugin(d.name, requiredPlugins)) continue;

            PluginMetadata depMetaData = plugins.get(d.name);
            if (depMetaData == null) {
                throw new UnableToResolveDependencies(
                    String.format("Unable to find dependency '%s' for '%s': plugin not found", d, p)
                );
            }

            optionalDependenciesOf(depMetaData, result, requiredPlugins, d.optional, depth + 1);
        }

        if (includeSelf) {
            if (!result.containsKey(depth)) {
                List<String> newLevelOptionalDependencies = new ArrayList<>();
                newLevelOptionalDependencies.add(p.getName());
                result.put(depth, newLevelOptionalDependencies);
            } else if (!result.get(depth).contains(p.getName())) {
                result.get(depth).add(p.getName());
            }
        }
    }

    /**
     * Assess whether an optional dependency is actually marked as required.
     *
     * @param pluginName name of optional dependency plugin
     * @param requiredPlugins list of required plugins
     * @return true if optional dependency is required to be installed. Otherwise, false.
     */
    private boolean optionalRequiredPlugin(String pluginName, Collection<PluginSpec> requiredPlugins) {
        for (PluginSpec plugin : requiredPlugins) {
            if (plugin.getName().equals(pluginName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Assess whether the dependency actually needs to be installed or upgraded.
     * 
     * @param jenkins top-level jenkins object
     * @param d the dependency
     * @return true if the dependency should be installed/upgraded. Otherwise, false.
     */
    private boolean shouldBeIncluded(Jenkins jenkins, Dependency d) {
        try {
            VersionNumber installedVersion = jenkins.getPlugin(d.name).getVersion();
            VersionNumber requiredVersion = new VersionNumber(d.version);
            return installedVersion.isOlderThan(requiredVersion);
        } catch (IllegalArgumentException ex) {
            // Plugin not installed
            return true;
        }
    }

    public static class UnableToResolveDependencies extends RuntimeException {

        public UnableToResolveDependencies(String format) {
            super(format);
        }
    }
}
