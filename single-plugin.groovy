// Run tests that requires plugins enumerated in TEST_ONLY_PLUGINS variable.
//
// 'TEST_ONLY_PLUGINS=git,envinject' run all tests that require git or envinject
// using WithPlugins annotations

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.jenkinsci.test.acceptance.junit.FilterRule.Filter;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.PluginManager.PluginSpec;

import java.lang.annotation.Annotation;

bind Filter toInstance new FilterImpl();

class FilterImpl extends Filter {
    public String whySkip(Statement base, Description desc) {
        for (annot in getAnnotations(desc, WithPlugins.class)) {
            for (value in annot.value()) {
                if (testOnlyPlugins().contains(new PluginSpec(value).name)) {
                    return null;
                }
            }
        }

        return "Running only tests for plugins: ${testOnlyPlugins()}";
    }

    private Collection<String> testOnlyPlugins() {
        return (System.getenv("TEST_ONLY_PLUGINS") ?: "").split(",").each { it.trim() };
    }
}
