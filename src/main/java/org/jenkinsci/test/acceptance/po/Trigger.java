package org.jenkinsci.test.acceptance.po;

/**
 * Base type for {@link PageAreaImpl} for trigger.
 * See {@link Job#addTrigger(Class)}.
 */
public abstract class Trigger extends PageAreaImpl {
    public final Control enabled = control("");

    protected Trigger(Job parent, String path) {
        super(parent, path);
    }
}
