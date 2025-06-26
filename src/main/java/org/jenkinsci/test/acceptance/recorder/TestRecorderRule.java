package org.jenkinsci.test.acceptance.recorder;

import org.jenkinsci.test.acceptance.utils.SystemEnvironmentVariables;

public final class TestRecorderRule {

    private static final String OFF = "off";
    private static final String FAILURES = "failuresOnly";
    private static final String ALWAYS = "always";

    private static final String DEFAULT_MODE = FAILURES;

    private static String RECORDER_OPTION = SystemEnvironmentVariables.getPropertyVariableOrEnvironment(
                    "RECORDER", DEFAULT_MODE)
            .trim();

    public static boolean isRecorderEnabled() {
        return !OFF.equals(RECORDER_OPTION);
    }

    public static boolean saveAllExecutions() {
        return ALWAYS.equals(RECORDER_OPTION);
    }
}
