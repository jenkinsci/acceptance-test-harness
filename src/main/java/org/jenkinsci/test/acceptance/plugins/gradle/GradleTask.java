package org.jenkinsci.test.acceptance.plugins.gradle;

public enum GradleTask {
    HELLO("hello", "Hello world!"),
    FIRST("firstTask", "First!"),
    SECOND("secondTask", "Second!"),
    JOB_PARAM_AS_PROJECT_PROPERTIES("jobParametersAsProjectProperties"),
    JOB_PARAM_AS_SYSTEM_PROPERTIES("jobParametersAsSystemProperties");

    private String name;
    private String println;

    GradleTask(final String name) {
        this.name = name;
    }

    GradleTask(final String name, final String println) {
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
