package org.jenkinsci.test.acceptance.utils;

/**
 * Gets a system environment variable. First it checks if the variable is set as Java system property  and if it is the case,
 * it returns the configured value.
 * If not then it checks if it is defined as environment variable, and if not it returns the default value.
 */
public class SystemEnvironmentVariables {

    public static String getPropertyVariableOrEnvironment(String property, String def) {

        String propertyValue = System.getProperty(property);

        if (propertyValue == null) {

            String envValue = System.getenv(property);
            if (envValue == null) {
                return def;
            }

            return envValue;
        } else {
            return propertyValue;
        }
    }
}
