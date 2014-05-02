package org.jenkinsci.test.acceptance.plugins.nodelabelparameter;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Parameter;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Node")
public class NodeParameter extends Parameter {
    public final Control allowMultiple = control("triggerIfResult[allowMultiSelectionForConcurrentBuilds]");

    public final WebElement allNodes = find(by.xpath("//option[text()[normalize-space(.)='All Nodes']]"));
    public final WebElement ignoreOffline = find(by.xpath("//option[text()[normalize-space(.)='Ignore Offline Nodes']]"));

    public NodeParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        for (String l : v.toString().split(",[ ]?")) {
            control("labels").select(l);
        }
    }
}
