package org.jenkinsci.test.acceptance.recorder;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.junit.GlobalRule;
import org.jenkinsci.test.acceptance.utils.SystemEnvironmentVariables;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@GlobalRule
public class HarRecorder extends TestWatcher {
    static String CAPTURE_HAR = SystemEnvironmentVariables.getPropertyVariableOrEnvironment("HAR", null);

    private static BrowserMobProxy proxy;

    public static BrowserMobProxy getBrowserMobProxy() {
        if (proxy == null) {
            // start the proxy
            proxy = new BrowserMobProxyServer();
            proxy.start(0);
            // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
            proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        }
        return proxy;
    }

    FailureDiagnostics diagnostics;

    @Inject
    public HarRecorder(FailureDiagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    public static boolean isCaptureHarEnabled() {
        return CAPTURE_HAR != null;
    }

    @Override
    protected void finished(Description description) {
        if (isCaptureHarEnabled() && proxy != null) {
            Har har = proxy.getHar();
            File file = diagnostics.touch("jenkins.har");
            try {
                har.writeTo(file);
            } catch (IOException e) {
                System.err.println("Unable to write HAR file to " + file);
                e.printStackTrace(System.err);
            }
        }
    }
}
