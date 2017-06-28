package org.jenkinsci.test.acceptance.plugins.gradle;

/**
 * Created by wlaschinger on 29.06.17.
 */
public enum  GradleTask {

    HELLO("hello", "Hello world!"),
    FIRST("firstTask", "First!"),
    SECOND("secondTask", "Second!"),
    ENVIRONMENT_VARIABLES("environmentVariables", null),
    JOB_PARAM_AS_PROJECT_PROPERTIES("jobParametersAsProjectProperties", null),
    JOB_PARAM_AS_SYSTEM_PROPERTIES("jobParametersAsSystemProperties", null);

    private String name;
    private String println;

    GradleTask(final String name, final String println){
        this.name = name;
        this.println = println;
    }

    public String getName() {
        return name;
    }

    public String getPrintln() {
        return println;
    }
}
