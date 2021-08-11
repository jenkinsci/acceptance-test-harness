package org.jenkinsci.test.acceptance.junit;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.Assert.assertNull;

public class WithSystemPropertiesTest {

    @Test
    public void combine_WithPlugins_annotations() throws Exception {
        assertThat(WithSystemProperties.RuleImpl.getProperties(FakeTestClass.class.getAnnotation(WithSystemProperties.class))
                , arrayContainingInAnyOrder("-Dproperty1=value1", "-Dproperty2=value2"));
        assertThat(WithSystemProperties.RuleImpl.getProperties(FakeTestClass.class.getMethod("test").getAnnotation(WithSystemProperties.class))
                , arrayContainingInAnyOrder("-Dproperty3=value3", "-Dproperty4=value4"));
        assertNull(WithSystemProperties.RuleImpl.getProperties(FakeTestClass.class.getMethod("another_test").getAnnotation(WithSystemProperties.class)));
        assertNull(WithSystemProperties.RuleImpl.getProperties(FakeTestClass.class.getMethod("bad_test").getAnnotation(WithSystemProperties.class)));
        assertThat(WithSystemProperties.RuleImpl.getProperties(FakeTestClass.class.getMethod("another_bad_test").getAnnotation(WithSystemProperties.class))
                , arrayContaining(""));
    }

    @WithSystemProperties({"-Dproperty1=value1", "-Dproperty2=value2",})
    public static final class FakeTestClass {
        @Test @WithSystemProperties({"-Dproperty3=value3", "-Dproperty4=value4"})
        public void test() { }
        @Test
        public void another_test() { }
        @Test @WithSystemProperties({})
        public void bad_test() { }
        @Test @WithSystemProperties({""})
        public void another_bad_test() { }

    }
}
