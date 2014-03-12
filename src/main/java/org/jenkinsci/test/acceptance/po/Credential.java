package org.jenkinsci.test.acceptance.po;

import java.net.URL;

/**
 * @author Vivek Pandey
 */
public abstract class Credential extends ContainerPageObject {

    protected Credential(Jenkins j, URL url) {
        super(j, url);
    }
}
