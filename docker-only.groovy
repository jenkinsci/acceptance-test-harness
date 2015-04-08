import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.jenkinsci.test.acceptance.junit.FilterRule.Filter;
import org.jenkinsci.test.acceptance.junit.WithDocker;

bind Filter toInstance ({ base, method, target ->
    def docker = !Filter.getAnnotations(method, target, WithDocker.class).isEmpty();
    if (docker) return null;

    return "Running only docker based tests";
} as Filter)
