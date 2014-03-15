package org.jenkinsci.test.acceptance.po;

/**
 * Different Jenkins model objects often share a common trait,
 * and for better reuse it makes sense to split them off into a separate page object.
 *
 * <p>
 * This class is mostly a marker super-type for such mix-in types.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class MixIn extends ContainerPageObject {
    protected MixIn(ContainerPageObject context) {
        super(context, context.url);
    }
}
