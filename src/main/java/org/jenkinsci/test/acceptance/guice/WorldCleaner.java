package org.jenkinsci.test.acceptance.guice;

import javax.inject.Singleton;

/**
 * {@link Cleaner} at the end of {@link Singleton}.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class WorldCleaner extends Cleaner {
}
