package org.jenkinsci.test.acceptance.utils;

/**
 * Gets a system environment variable. First it checks if the variable is an environment variable and if it is the case,
 * it returns the configured value.
 * If not then it checks if it is defined as java system property, and if not it returns the default value.
 */
public class SystemEnvironmentVariables {

    public static final String getPropertyVariable(String property, String def) {
        return System.getProperty(property, def);
    }

    public static final String getEnvironmentVariable(String property) {
        return System.getenv(property);
    }

    public static final String getEnvironmentOrPropertyVariable(String property, String def) {

        String environmentVariable = getEnvironmentVariable(property);

        if (environmentVariable == null) {
            return getPropertyVariable(property, def);
        }

        return environmentVariable;

    }

}
