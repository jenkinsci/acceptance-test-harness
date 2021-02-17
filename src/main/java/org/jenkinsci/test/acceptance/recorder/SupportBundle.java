package org.jenkinsci.test.acceptance.recorder;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.utils.SupportBundleRequest;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SupportBundle extends TestWatcher {
    private static final Logger LOGGER = Logger.getLogger(SupportBundle.class.getName());

    private static class SupportBundleSpec {
        private Jenkins instance;
        private SupportBundleRequest request;

        public SupportBundleSpec(Jenkins instance, SupportBundleRequest request) {
            this.instance = instance;
            this.request = request;
        }
    }

    private List<SupportBundleSpec> specs = new ArrayList<>();

    public SupportBundle() {}

    public void addSpec(Jenkins jenkins, SupportBundleRequest request) {
        specs.add(new SupportBundleSpec(jenkins, request));
    }

    @Override
    protected void failed(Throwable e, Description description) {
        for (SupportBundleSpec spec : specs) {
            try {
                spec.instance.getPlugin("support-core");
                spec.instance.generateSupportBundle(spec.request);
            } catch (IllegalArgumentException _) {
                LOGGER.info("support-core plugin not installed, skipping support bundle");
            }
        }
    }
}
