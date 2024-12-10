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
package org.jenkinsci.test.acceptance.plugins.scriptler;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Node;

public class ScriptResult {
    private static final List<String> BUILT_IN_NODE_NAMES = List.of("built-in", "controller", "master");
    private final String result;

    public ScriptResult(String result) {
        this.result = result;
    }

    public String output(Node node) {
        String name = node.getName();
        if (node instanceof Jenkins) {
            // TODO: use the below code once Scriptler versions 390 and up are the only ones tested
            // return "(" + node + ")";
            return BUILT_IN_NODE_NAMES.stream()
                    .map(nodeName -> "(" + nodeName + ")")
                    .map(this::output)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return output(name);
    }

    private String output(String node) {
        Pattern pattern = Pattern.compile(
                "^_+\\n\\[" + Pattern.quote(node) + "\\]:\\n(.*?)\\n_+$", Pattern.DOTALL | Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(result);

        return matcher.find() ? matcher.group(1) : null;
    }
}
