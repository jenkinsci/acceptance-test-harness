import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.jenkinsci.test.acceptance.junit.FilterRule.Filter;
import org.jenkinsci.test.acceptance.junit.Native;

bind Filter toInstance ({ base, method, target ->
    for (annot in Filter.getAnnotations(method, target, Native.class)) {
        if ('docker' in annot.value()) {
            return null;
        }
    }

    return "Running only docker based tests";
} as Filter)
