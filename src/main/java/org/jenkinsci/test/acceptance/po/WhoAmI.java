package org.jenkinsci.test.acceptance.po;

/**
 * Who Am I page in Jenkins
 */
public class WhoAmI extends ContainerPageObject {

    public WhoAmI(ContainerPageObject parent) {
        super(parent, parent.url("whoAmI/"));
    }
}
