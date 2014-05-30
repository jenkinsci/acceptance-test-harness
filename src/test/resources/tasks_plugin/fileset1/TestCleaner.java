//
// +-----------------------------------------------------+
// |              =========================              |
// |              !  W  A  R  N  I  N  G  !              |
// |              =========================              |
// |                                                     |
// | This file is  N O T   P A R T  of the jenkins       |
// | acceptance test harness project's source code!      |
// |                                                     |
// | This file is only used for testing purposes w.r.t   |
// | the task scanner plugin test.                       |
// |                                                     |
// +-----------------------------------------------------+
//


package org.jenkinsci.test.acceptance.guice;

import com.google.inject.Inject;

/**
 * {@link Cleaner} at the end of each {@link TestScope}.
 *
 * Oftentimes marking your class with {@link AutoCleaned} gets the job done.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class TestCleaner extends Cleaner {
    @Inject
    TestLifecycle lifecycle;

    @Override
    void performCleanUp() {
        super.performCleanUp();
        for (Object o : lifecycle.getInstances()) {
            if (o instanceof AutoCleaned) {
                try {
                    ((AutoCleaned)o).close();
                } catch (Throwable t) {
                    // just log and move on so that other cleaners can run
                    System.out.println(o+" clean up failed");
                    t.printStackTrace();
                }
            }
        }
    }
}
