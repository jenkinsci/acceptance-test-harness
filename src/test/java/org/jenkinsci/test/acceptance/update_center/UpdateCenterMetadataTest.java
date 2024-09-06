package org.jenkinsci.test.acceptance.update_center;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.util.VersionNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Test;

/**
 * @author ogondza.
 */
public class UpdateCenterMetadataTest {

    public static final List<Dependency> NO_DEPS = List.of();

    private Jenkins jenkins = mock(Jenkins.class);

    {
        when(jenkins.getVersion()).thenReturn(new VersionNumber("2"));
        when(jenkins.getPlugin(any(String.class))).thenThrow(new IllegalArgumentException("Not installed"));
    }

    private HashMap<String, PluginMetadata> plugins = new HashMap<>();

    {
        plugins.put("standalone", new PluginMetadata("standalone", "jenkins:standalone:1", "1", "1", NO_DEPS));

        plugins.put("provider", new PluginMetadata("provider", "jenkins:provider:1", "1", "1", NO_DEPS));
        plugins.put(
                "consumer",
                new PluginMetadata("consumer", "jenkins:consumer:1", "1", "1", List.of(new Dependency("provider:1"))));

        plugins.put(
                "complex",
                new PluginMetadata(
                        "complex",
                        "jenkins:complex:1",
                        "1",
                        "1",
                        List.of(new Dependency("branchb:1"), new Dependency("brancha:1"))));
        plugins.put(
                "brancha",
                new PluginMetadata("brancha", "jenkins:brancha:1", "1", "1", List.of(new Dependency("depa:1"))));
        plugins.put("depa", new PluginMetadata("depa", "jenkins:depa:1", "1", "1", NO_DEPS));
        plugins.put(
                "branchb",
                new PluginMetadata(
                        "branchb",
                        "jenkins:branchb:1",
                        "1",
                        "1",
                        List.of(new Dependency("depb0:1"), new Dependency("depb1:1"))));
        plugins.put("depb0", new PluginMetadata("depb0", "jenkins:depb0:1", "1", "1", NO_DEPS));
        plugins.put("depb1", new PluginMetadata("depb1", "jenkins:depb1:1", "1", "1", NO_DEPS));
    }

    private UpdateCenterMetadata ucm = UpdateCenterMetadata.get("id", plugins);

    @Test
    public void transitiveDependenciesOfSimple() throws Exception {

        assertThat(ucm.transitiveDependenciesOf(jenkins, specs()), Matchers.<PluginMetadata>emptyIterable());
        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("standalone")),
                Matchers.contains(plugins.get("standalone")));

        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("provider")), Matchers.contains(plugins.get("provider")));
        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("consumer")),
                Matchers.contains(plugins.get("provider"), plugins.get("consumer")));

        List<PluginMetadata> complexDeps = ucm.transitiveDependenciesOf(jenkins, specs("complex"));

        int complexIndex = complexDeps.indexOf(plugins.get("complex"));
        int branchAIndex = complexDeps.indexOf(plugins.get("brancha"));
        int branchBIndex = complexDeps.indexOf(plugins.get("branchb"));
        int depAIndex = complexDeps.indexOf(plugins.get("depa"));
        int depB0Index = complexDeps.indexOf(plugins.get("depb0"));
        int depB1Index = complexDeps.indexOf(plugins.get("depb1"));
        assertThat(complexIndex, Matchers.greaterThanOrEqualTo(branchAIndex));
        assertThat(complexIndex, Matchers.greaterThanOrEqualTo(branchBIndex));
        assertThat(branchAIndex, Matchers.greaterThanOrEqualTo(depAIndex));
        assertThat(branchBIndex, Matchers.greaterThanOrEqualTo(depB0Index));
        assertThat(branchBIndex, Matchers.greaterThanOrEqualTo(depB1Index));
    }

    @Test
    public void transitiveDependenciesOfExplicitOrder() throws Exception {
        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("consumer", "provider")),
                Matchers.contains(plugins.get("provider"), plugins.get("consumer")));
        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("provider", "consumer")),
                Matchers.contains(plugins.get("provider"), plugins.get("consumer")));

        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("consumer@1", "provider")),
                Matchers.contains(plugins.get("provider"), plugins.get("consumer")));
        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("provider", "consumer@1")),
                Matchers.contains(plugins.get("provider"), plugins.get("consumer")));
        assertThat(
                ucm.transitiveDependenciesOf(jenkins, specs("provider@1", "consumer@1")),
                Matchers.contains(plugins.get("provider"), plugins.get("consumer")));
    }

    private List<PluginSpec> specs(String... specs) {
        ArrayList<PluginSpec> ret = new ArrayList<>(specs.length);
        for (String spec : specs) {
            ret.add(new PluginSpec(spec));
        }
        return ret;
    }
}
