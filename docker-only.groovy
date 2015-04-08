import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.jenkinsci.test.acceptance.junit.FilterRule.Filter;
import org.jenkinsci.test.acceptance.junit.WithDocker;

bind Filter toInstance ({ base, desc ->
    def docker = !Filter.getAnnotations(desc, WithDocker.class).isEmpty();
    if (docker) return null;

    return "Running only docker based tests";
} as Filter)
