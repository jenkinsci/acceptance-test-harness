package org.jenkinsci.test.acceptance.junit;

/**
 * Marker interface to identify a smoke test. A smoke test is a test that basically uses one important aspect of the
 * acceptance testing framework. Run these smoke tests in order to get a first impression if a framework change did not
 * break anything. The overall number of smoke tests should be less than 10.
 *
 * @author Ullrich Hafner
 */
public interface SmokeTest {
    // marker interface
}
