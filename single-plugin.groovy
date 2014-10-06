// Run tests that requires plugins enumerated in RUN_ONLY_PLUGINS variable.
//
// 'RUN_ONLY_PLUGINS=git,envinject' run all tests that require git or envinject
// using WithPlugins annotations

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.jenkinsci.test.acceptance.junit.FilterRule.Filter;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.PluginManager.PluginSpec;

import java.lang.annotation.Annotation;

bind Filter toInstance new FilterImpl();

class FilterImpl extends Filter {
    public String whySkip(Statement base, FrameworkMethod method, Object target) {
        for (annot in getAnnotations(method, target, WithPlugins.class)) {
            for (value in annot.value()) {
                if (runOnlyPlugins().contains(new PluginSpec(value).name)) {
                    return null;
                }
            }
        }

        return "Running only tests for plugins: ${runOnlyPlugins()}";
    }

    private Collection<String> runOnlyPlugins() {
        return (System.getenv("RUN_ONLY_PLUGINS") ?: "").split(",").each { it.trim() };
    }
}
