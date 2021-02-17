package org.jenkinsci.test.acceptance.recorder;

import org.jenkinsci.test.acceptance.guice.World;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.utils.SupportBundleRequest;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SupportBundle extends TestWatcher {
    private static final Logger LOGGER = Logger.getLogger(SupportBundle.class.getName());

    @Inject
    private FailureDiagnostics diagnostics;

    private Map<String,Jenkins> spec = new HashMap<>();

    public SupportBundle() {}

    public void setSpec(@Nonnull Map<String, Jenkins> spec) {
        this.spec.putAll(spec);
    }

    @Override
    protected void failed(Throwable e, Description description) {
        World.get().getInjector().injectMembers(this);
        for (Map.Entry<String, Jenkins> entry: spec.entrySet()) {
            try {
                entry.getValue().getPlugin("support-core");
                File file = diagnostics.touch(entry.getKey());
                entry.getValue().generateSupportBundle(SupportBundleRequest.builder().includeDefaultComponents().setOutputFile(file).build());
            } catch (IllegalArgumentException _) {
                LOGGER.info("support-core plugin not installed, skipping support bundle");
            }
        }
    }
}
