package org.jenkinsci.test.acceptance.recorder;

import jakarta.inject.Inject;
import java.util.logging.Logger;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.junit.GlobalRule;
import org.jenkinsci.test.acceptance.recorder.har.BiDiHARRecorder;
import org.jenkinsci.test.acceptance.utils.SystemEnvironmentVariables;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.devtools.HasDevTools;

/**
 * The system property RECORD_BROWSER_TRAFFIC can be set to either off, failuresOnly or always to control when browser
 * traffic should be recorded when launching tests.
 * Traffic is recorded as a HAR (<a href="https://en.wikipedia.org/wiki/HAR_(file_format)">HAR file format</a>) file based on all network interactions between the
 * browser and the Jenkins instance and then add it as JUnit attachment to the test result.
 */
@GlobalRule
public class HarRecorder extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(HarRecorder.class.getName());

    public enum State {
        OFF("off", false, false),
        FAILURES_ONLY("failuresOnly", true, false),
        ALWAYS("always", true, true);

        private final String value;
        private final boolean saveOnSuccess;
        private final boolean saveOnFailure;

        State(String value, boolean saveOnFailure, boolean saveOnSuccess) {
            this.value = value;
            this.saveOnFailure = saveOnFailure;
            this.saveOnSuccess = saveOnSuccess;
        }

        public boolean isSaveOnSuccess() {
            return saveOnSuccess;
        }

        public boolean isSaveOnFailure() {
            return saveOnFailure;
        }

        public boolean isRecordingEnabled() {
            return saveOnFailure || saveOnSuccess;
        }

        public String getValue() {
            return value;
        }

        public static State value(String value) {
            for (State s : values()) {
                if (s.value.equals(value)) {
                    return s;
                }
            }
            return FAILURES_ONLY;
        }
    }

    static State CAPTURE_HAR = State.value(SystemEnvironmentVariables.getPropertyVariableOrEnvironment(
            "RECORD_BROWSER_TRAFFIC", State.OFF.getValue()));

    @Inject
    protected WebDriver driver;

    private FailureDiagnostics diagnostics;
    private Network network;
    private BiDiHARRecorder harRecorder;

    @Inject
    public HarRecorder(FailureDiagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    public static boolean isCaptureHarEnabled() {
        return CAPTURE_HAR.isRecordingEnabled();
    }

    @Override
    protected void succeeded(Description description) {
        if (CAPTURE_HAR.isSaveOnSuccess()) {
            recordHar();
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (CAPTURE_HAR.isSaveOnFailure()) {
            recordHar();
        }
    }

    @Override
    protected void starting(Description description) {
        if (!CAPTURE_HAR.isRecordingEnabled()) {
            return;
        } 
        initializeHarForTest(description.getDisplayName());
    }

    private void initializeHarForTest(String name) {
        if (!(driver instanceof HasBiDi)) {
            LOGGER.warning("configured driver does not support BiDi, HAR recording will not be available");
            return;
        }
        if (driver instanceof HasDevTools) {
            // selenium removed support for DevTools for firefox
            LOGGER.fine("configured driver supports DevTools!");
        }

        
        if (driver instanceof HasCapabilities) {
            Capabilities caps = ((HasCapabilities) driver).getCapabilities();
            harRecorder = new BiDiHARRecorder(name, caps.getBrowserName(), caps.getBrowserVersion());
        } else {
            harRecorder = new BiDiHARRecorder(name);
        }
        
        network = new Network(driver);
        network.onBeforeRequestSent(harRecorder::onBeforeRequestSent);
        network.onResponseCompleted(harRecorder::onResponseCompleted);
    }

    private void recordHar() {
        try {
            
        } finally {
            // TODO do we need to close this or is it closed when the driver is closed?
            // we also do not close if we do not write the HAR file, so if we need to close it we would leak
            network.close();
        }
    }
    
}
