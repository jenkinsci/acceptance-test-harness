package org.jenkinsci.test.acceptance.utils;

import org.openqa.selenium.json.Json;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SupportBundleRequest {
    private final File outputFile;

    private final Payload payload;

    private SupportBundleRequest(File outputFile, Payload payload) {
        this.outputFile = outputFile;
        this.payload = payload;
    }

    public String getJsonParameter() {
        return new Json().toJson(payload);
    }

    public File getOutputFile() {
        return outputFile;
    }

    // https://github.com/jenkinsci/support-core-plugin/blob/25ddb2f2fec34068c098b8b42cd682d38de9c36e/src/main/java/com/cloudbees/jenkins/support/SupportAction.java#L231-L249
    public static class Selection {
        private final String name;
        private final boolean selected;


        public Selection(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return selected;
        }
    }

    public static class Payload{
        private final List<Selection> components;

        public Payload(List<Selection> components) {
            this.components = components;
        }

        public List<Selection> getComponents() {
            return components;
        }
    }

    public static class Builder {
        private File outputFile;

        private Set<String> includedComponents = new HashSet<>();

        private Set<String> excludedComponents = new HashSet<>();

        public Builder setOutputFile(File outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        public Builder includeComponents(String... components) {
            for (String c : components) {
                includedComponents.add(c);
            }
            return this;
        }

        public Builder excludeComponents(String... components) {
            for (String c : components) {
                excludedComponents.add(c);
            }
            return this;
        }

        public Builder includeDefaultComponents() {
            return includeComponents("AgentsConfigFile",
                    "ConfigFileComponent",
                    "OtherConfigFilesComponent",
                    "AboutBrowser",
                    "AboutJenkins",
                    "AboutUser",
                    "AdministrativeMonitors",
                    "BuildQueue",
                    "DumpExportTable",
                    "EnvironmentVariables",
                    "FileDescriptorLimit",
                    "GCLogs",
                    "HeapUsageHistogram",
                    "ItemsContent",
                    "Agents",
                    "Master",
                    "JenkinsLogs",
                    "LoadStats",
                    "LoggerManager",
                    "Metrics",
                    "NetworkInterfaces",
                    "NodeMonitors",
                    "ReverseProxy",
                    "RootCAs",
                    "RunningBuilds",
                    "SlaveCommandStatistics",
                    "SlaveLogs",
                    "SystemProperties",
                    "ThreadDumps",
                    "UpdateCenter",
                    "ComponentImpl",
                    "SlowRequestComponent",
                    "DeadlockRequestComponent");
        }

        private Payload buildPayload() {
            List<Selection> components = new ArrayList<>();
            for (String i : includedComponents) {
                components.add(new Selection(i, true));
            }
            for (String x : excludedComponents) {
                components.add(new Selection(x, false));
            }
            return new Payload(components);
        }

        public SupportBundleRequest build() {
            return new SupportBundleRequest(outputFile, buildPayload());
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
