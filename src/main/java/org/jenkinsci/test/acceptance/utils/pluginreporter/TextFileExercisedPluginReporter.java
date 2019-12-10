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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 * Exercised Plugin Reporter that logs to text file
 * The contents are java properties.
 *
 * Properties will be stored as follows:
 *
 * <testName>::<pluginName> = <pluginVersion>
 *
 * @author scott.hebert@ericsson.com
 */
public class TextFileExercisedPluginReporter implements ExercisedPluginsReporter {

    private static TextFileExercisedPluginReporter instance = null;
    private static final Logger LOGGER = Logger.getLogger(TextFileExercisedPluginReporter.class.getName());
    private File file;

    private TextFileExercisedPluginReporter() {
        file = new File(System.getProperty("basedir") + "/target/exercised-plugins.properties");
        if (file.exists()) {
            LOGGER.info("Deleting " + file.getAbsolutePath());
            file.delete();
        }
        try {
            FileUtils.touch(file);
         } catch (IOException e) {
             LOGGER.severe(e.getMessage());
        }

    }
    public static TextFileExercisedPluginReporter getInstance() {
        if (instance == null) {
            instance = new TextFileExercisedPluginReporter();
        }
        return instance;
    }
    @Override
    public void log(String testName, String pluginName, String pluginVersion) {

        PropertiesConfiguration config;
        try {
           config = new PropertiesConfiguration(file);
        } catch (ConfigurationException e) {
            LOGGER.severe(e.getMessage());
            return;
        }

        config.setProperty(testName + "$" + pluginName, pluginVersion);
        try {
            config.save();
        } catch (ConfigurationException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}
