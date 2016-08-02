package org.jenkinsci.test.acceptance.steps;

import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.View;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Cucumber step definitions often rely on various contextual "it" objects
 * to act on.
 *
 * In the original ruby version, those are accessed just as instance variables
 * from build steps. But in Java, we need typed definitions. This class captures
 * those contextual values.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class Context {
    @Inject
    Jenkins jenkins;
    Job job;
    BuildStep step;
    View view;
    ArtifactArchiver artifactArchiver;
}
