package org.jenkinsci.test.acceptance.recorder;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.Har;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.junit.GlobalRule;
import org.jenkinsci.test.acceptance.utils.SystemEnvironmentVariables;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import static org.jenkinsci.test.acceptance.recorder.HarRecorder.State.*;

/**
 * The system property RECORD_BROWSER_TRAFFIC can be set to either off, failuresOnly or always to control when browser
 * traffic should be recorded when launching tests.
 * Traffic is recorded as a HAR (<a href="https://en.wikipedia.org/wiki/HAR_(file_format)">HAR file format</a>) file based on all network interactions between the
 * browser and the Jenkins instance and then add it as JUnit attachment to the test result.
 */
@GlobalRule
public class HarRecorder extends TestWatcher {
    public enum State {
        OFF("off", false, false), FAILURES_ONLY("failuresOnly", true, false), ALWAYS("always", true, true);

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

    static State CAPTURE_HAR = value(SystemEnvironmentVariables.getPropertyVariableOrEnvironment("RECORD_BROWSER_TRAFFIC", FAILURES_ONLY.getValue()));

    private static BrowserUpProxy proxy;

    /**
     * Create a proxy to record the HAR listening on the specified address
     * @param networkAddress the specific address to bind to, or {@code null} to bind on all addresses
     */
    public static BrowserUpProxy getProxy(InetAddress networkAddress) {
        if (proxy == null) {
            // start the proxy
            proxy = new BrowserUpProxyServer();
            // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
            proxy.enableHarCaptureTypes(
                    CaptureType.REQUEST_HEADERS,
                    CaptureType.REQUEST_CONTENT,
                    CaptureType.RESPONSE_HEADERS,
                    CaptureType.RESPONSE_CONTENT
            );
            proxy.setTrustAllServers(true);
            proxy.setMitmDisabled(true);
            proxy.start(0, networkAddress);
        }
        return proxy;
    }

    private FailureDiagnostics diagnostics;

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

    private void recordHar() {
        if (proxy != null) {
            proxy.stop();
            Har har = proxy.getHar();
            File file = diagnostics.touch("jenkins.har");
            try {
                har.writeTo(file);
            } catch (IOException e) {
                System.err.println("Unable to write HAR file to " + file);
                e.printStackTrace(System.err);
            }
            proxy = null;
        }
    }
}
