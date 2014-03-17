package org.jenkinsci.test.acceptance.update_center;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class UpdateCenterMetadata {
    private final JsonNode tree;

    /**
     * .json or .json.html file served from update center.
     */
    public UpdateCenterMetadata(File data) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(data));
        r.readLine();   // the first line is preamble
        String json = r.readLine(); // the 2nd line is the actual JSON
        r.readLine();   // the third line is postamble

        tree = new ObjectMapper().readTree(json);
    }

    public PluginMetadata getPlugin(String name) {
        JsonNode n = tree.get("plugin").get(name);
        if (n==null)        return null;

        return new PluginMetadata(n);
    }
}
