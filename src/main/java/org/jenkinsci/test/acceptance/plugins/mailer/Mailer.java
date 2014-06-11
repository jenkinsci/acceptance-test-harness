package org.jenkinsci.test.acceptance.plugins.mailer;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("E-mail Notification")
public class Mailer extends AbstractStep implements PostBuildStep {
    public final Control recipients = control("recipients", "mailer_recipients");

    public Mailer(Job parent, String path) {
        super(parent, path);
    }
}
