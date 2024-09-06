package org.jenkinsci.test.acceptance.plugins.cobertura;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Cobertura report on job or page.
 *
 * @author Kohsuke Kawaguchi
 */
public class CoberturaAction extends ContainerPageObject {
    private final ContainerPageObject parent;

    public CoberturaAction(ContainerPageObject parent) { // Build or Job
        super(parent, parent.url("cobertura/"));
        this.parent = parent;
    }

    public int getPackageCoverage() {
        return readInt("//th[text()=\"Packages\"]/../td");
    }

    public int getFilesCoverage() {
        return readInt("//th[text()=\"Files\"]/../td");
    }

    public int getClassesCoverage() {
        return readInt("//th[text()=\"Classes\"]/../td");
    }

    public int getMethodsCoverage() {
        return readInt("//th[text()=\"Methods\"]/../td");
    }

    public int getLinesCoverage() {
        return readInt("//th[text()=\"Lines\"]/../td");
    }

    public int getConditionalsCoverage() {
        return readInt("//th[text()=\"Conditionals\"]/../td");
    }

    private int readInt(String xpath) {
        open();
        return Integer.parseInt(find(by.xpath(xpath)).getText().replace("%", ""));
    }
}
