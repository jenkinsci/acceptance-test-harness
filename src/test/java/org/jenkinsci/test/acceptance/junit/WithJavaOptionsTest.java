package org.jenkinsci.test.acceptance.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class WithJavaOptionsTest {

    @Test
    public void combine_WithPlugins_annotations() throws Exception {
        assertThat(WithJavaOptions.RuleImpl.getOptions(FakeTestClass.class.getAnnotation(WithJavaOptions.class))
                , arrayContainingInAnyOrder("-Dproperty1=value1", "-Dproperty2=value2"));
        assertThat(WithJavaOptions.RuleImpl.getOptions(FakeTestClass.class.getMethod("test").getAnnotation(WithJavaOptions.class))
                , arrayContainingInAnyOrder("-Dproperty3=value3", "-Dproperty4=value4"));
        assertNull(WithJavaOptions.RuleImpl.getOptions(FakeTestClass.class.getMethod("another_test").getAnnotation(WithJavaOptions.class)));
        assertNull(WithJavaOptions.RuleImpl.getOptions(FakeTestClass.class.getMethod("bad_test").getAnnotation(WithJavaOptions.class)));
        assertThat(WithJavaOptions.RuleImpl.getOptions(FakeTestClass.class.getMethod("another_bad_test").getAnnotation(WithJavaOptions.class))
                , arrayContaining(""));
    }

    @WithJavaOptions({"-Dproperty1=value1", "-Dproperty2=value2",})
    public static final class FakeTestClass {
        @Test @WithJavaOptions({"-Dproperty3=value3", "-Dproperty4=value4"})
        public void test() { }
        @Test
        public void another_test() { }
        @Test @WithJavaOptions({})
        public void bad_test() { }
        @Test @WithJavaOptions({""})
        public void another_bad_test() { }

    }
}
