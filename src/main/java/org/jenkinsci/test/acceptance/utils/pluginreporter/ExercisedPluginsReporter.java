/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
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

package org.jenkinsci.test.acceptance.utils.pluginreporter;

import org.jenkinsci.test.acceptance.junit.WithPlugins;

/**
 * Interface for the ability to create a report to show plugins
 * and their versions that were exercised during the test suite run
 *
 * @author scott.hebert@ericsson.com
 */
public interface ExercisedPluginsReporter {
    /**
     * This method is called by {@link WithPlugins} to log and report
     * the plugin and its version installed by the test
     *
     * @param testName Name of Test being executed
     * @param pluginName Name of Plugin that was installed
     * @param pluginVersion Version of Plugin that was installed
     */
    void log(String testName, String pluginName, String pluginVersion);
}
