package org.jenkinsci.test.acceptance.recorder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.utils.SupportBundleRequest;
import org.jenkinsci.test.acceptance.utils.SystemEnvironmentVariables;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class SupportBundle extends TestWatcher {
    private static final Logger LOGGER = Logger.getLogger(SupportBundle.class.getName());

    private static Boolean CAPTURE_SUPPORT_BUNDLE = Boolean.parseBoolean(
            SystemEnvironmentVariables.getPropertyVariableOrEnvironment("CAPTURE_SUPPORT_BUNDLE", "true"));

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
        if (CAPTURE_SUPPORT_BUNDLE) {
            for (SupportBundleSpec spec : specs) {
                try {
                    spec.instance.getPlugin("support-core");
                    spec.instance.generateSupportBundle(spec.request);
                } catch (IllegalArgumentException ex) {
                    LOGGER.info("support-core plugin not installed, skipping support bundle");
                }
            }
        } else {
            LOGGER.info("Support bundle collection disabled.");
        }
    }
}
