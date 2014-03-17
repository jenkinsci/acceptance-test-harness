package org.jenkinsci.test.acceptance.update_center;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.spi.Dependency;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    private final JsonNode json;

    PluginMetadata(JsonNode json) {
        this.json = json;
    }

    URL getURL() throws MalformedURLException {
        return new URL(json.get("url").asText());
    }

    List<Dependency> getDependencies() {
        List<Dependency> r = new ArrayList<>();
        for (JsonNode d : json.get("dependencies")) {

        }
    }

}
