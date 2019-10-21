package org.jenkinsci.test.acceptance.machine;

import com.google.inject.Provider;
import org.jenkinsci.test.acceptance.controller.JenkinsController;

@Deprecated
public abstract class JenkinsProvider implements Provider<JenkinsController> {
}
