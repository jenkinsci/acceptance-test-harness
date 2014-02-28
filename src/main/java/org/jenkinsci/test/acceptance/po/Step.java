package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

/**
 * Common part of {@link BuildStep} and {@link PostBuildStep}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Step extends PageArea {
    protected Step(Injector injector, String path) {
        super(injector,path);
    }
}
