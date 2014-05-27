package org.jenkinsci.test.acceptance.plugins.nodelabelparameter;

import java.util.ArrayList;
import java.util.List;

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
    public final Control runIfSuccess = control("triggerIfResult[success]");
    public final Control runIfUnstable = control("triggerIfResult[unstable]");
    public final Control runAllCases = control("triggerIfResult[allCases]");

    public final Control allowMultiple = control("triggerIfResult[allowMultiSelectionForConcurrentBuilds]");
    public final Control disallowMultiple = control("triggerIfResult[multiSelectionDisallowed]");

    public final WebElement defaultNodesSelection = find(by.xpath("//select[@name='defaultSlaves']"));
    public final Control allowedNodes = control("allowedSlaves");

    public final WebElement allNodes = find(by.xpath("//option[text()[normalize-space(.)='All Nodes']]"));
    public final WebElement ignoreOffline = find(by.xpath("//option[text()[normalize-space(.)='Ignore Offline Nodes']]"));
    public final WebElement ignoreTempOffline = find(by.xpath("//option[text()[normalize-space(.)='Ignore Temp Offline Nodes']]"));

    public NodeParameter(Job job, String path) {
        super(job, path);
    }

    public List<WebElement> getPossibleNodesOptions(){ return allowedNodes.resolve().findElements(by.tagName("option")); }
    public List<WebElement> getDefaultNodeOptions(){ return defaultNodesSelection.findElements(by.tagName("option")); }

    @Override
    public void fillWith(Object v) {
        for (String l : v.toString().split(",[ ]?")) {
            control("labels").select(l);
        }
    }

    public List<String> applicableNodes() {
        List<String> nodes = new ArrayList<>();
        for (WebElement slave: control("labels").resolve().findElements(by.tagName("option"))) {
            nodes.add(slave.getText());
        }
        return nodes;
    }
}
