/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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
package plugins;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.HashMap;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.scriptler.Script;
import org.jenkinsci.test.acceptance.plugins.scriptler.ScriptResult;
import org.jenkinsci.test.acceptance.plugins.scriptler.Scriptler;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;

@WithPlugins("scriptler")
public class ScriptlerPluginTest extends AbstractJUnitTest {

    private final Resource SIMPLE_SCRIPT = resource("/scriptler_plugin/hello_world.groovy");
    private final Resource PARAMETERIZED_SCRIPT = resource("/scriptler_plugin/hello_parameterized.groovy");

    @Inject
    private SlaveController slaves;

    private Scriptler scriptler;

    @Before
    public void setup() {
        scriptler = jenkins.action(Scriptler.class);
    }

    @Test
    public void run_new_script() {
        Script script = scriptler.upload(SIMPLE_SCRIPT);
        String output = script.run().output(jenkins);
        assertThat(output, containsString("Hello world!"));
    }

    @Test
    public void run_new_parameterized_script() {
        Script script = scriptler.upload(PARAMETERIZED_SCRIPT);

        HashMap<String, String> params = new HashMap<>();
        params.put("noun", "space");
        String output = script.run(params).output(jenkins);
        assertThat(output, containsString("Hello space!"));
    }

    @Test
    public void delete_script() {
        Script simple = scriptler.upload(SIMPLE_SCRIPT);
        Script params = scriptler.upload(PARAMETERIZED_SCRIPT);

        params.delete();

        assertTrue(simple.exists());
        assertFalse(params.exists());
    }

    @Test
    public void run_on_slave() throws Exception {
        Slave slave = slaves.install(jenkins).get();

        Script script = scriptler.upload(SIMPLE_SCRIPT);
        String output = script.runOn(slave).output(slave);
        assertThat(output, equalTo("Hello world!"));
    }

    @Test
    public void run_on_master() throws Exception {
        slaves.install(jenkins).get();

        Script script = scriptler.upload(SIMPLE_SCRIPT);
        String output = script.run().output(jenkins);
        assertThat(output, equalTo("Hello world!"));
    }

    @Test
    public void run_on_all_nodes() throws Exception {
        Slave slave = slaves.install(jenkins).get();

        Script script = scriptler.upload(SIMPLE_SCRIPT);
        ScriptResult output = script.runOnAllNodes();

        assertThat(output.output(jenkins), equalTo("Hello world!"));
        assertThat(output.output(slave), equalTo("Hello world!"));
    }

    @Test
    public void run_on_all_slaves() throws Exception {
        Slave slaveA = slaves.install(jenkins).get();
        Slave slaveB = slaves.install(jenkins).get();

        Script script = scriptler.upload(SIMPLE_SCRIPT);
        ScriptResult output = script.runOnAllSlaves();

        assertThat(output.output(jenkins), nullValue());
        assertThat(output.output(slaveA), equalTo("Hello world!"));
        assertThat(output.output(slaveB), equalTo("Hello world!"));
    }
}
