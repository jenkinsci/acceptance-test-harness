/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.junit;

import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ogondza.
 */
public class WithPluginsTest {
    @Test
    public void combine_WithPlugins_annotations() throws Exception {
        // TODO test annotation harvesting and test class inheritance
        List<WithPlugins> annotations = Arrays.asList(
                FakeTestClass.class.getAnnotation(WithPlugins.class),
                FakeTestClass.class.getMethod("test").getAnnotation(WithPlugins.class)
        );

        List<PluginSpec> actual = WithPlugins.RuleImpl.combinePlugins(annotations);
        List<PluginSpec> expected = Arrays.asList(
                new PluginSpec("keep"),
                new PluginSpec("keepv@1"),
                new PluginSpec("specify@42"), // any version specification is more strict then plugin presence requirement
                new PluginSpec("keepspecific@42"), // ditto
                new PluginSpec("override@2"), // never version is the more specific one
                new PluginSpec("donotoverride@2"), // ditto
                new PluginSpec("inherit"),
                new PluginSpec("add")
        );
        assertThat(actual, equalTo(expected));
    }

    @WithPlugins(          {"keep", "keepv@1", "specify",    "keepspecific@42", "override@1", "donotoverride@2", "inherit"})
    private static final class FakeTestClass {
        @Test @WithPlugins({"keep", "keepv@1", "specify@42", "keepspecific",    "override@2", "donotoverride@1", "add"})
        public void test() throws Exception {

        }
    }
}
