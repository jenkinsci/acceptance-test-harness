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

    public final Control runIfSuccess = control(by.checkbox("Run next build only if build succeeds"));
    public final Control runIfUnstable = control(by.checkbox("Run next build only if build succeeds or is unstable"));
    public final Control runAllCases = control(by.checkbox("Run next build regardless of build result"));

    public final Control allowMultiple = control(by.checkbox("Allow multi node selection for concurrent builds"));
    public final Control disallowMultiple = control(by.checkbox("Disallow multi node selection when triggering build manually"));

    public final Control defaultNodes = control("defaultSlaves");
    public final Control allowedNodes = control("allowedSlaves");

    public final Control eligibility = control("");

    public NodeParameter(Job job, String path) {
        super(job, path);
    }

    public List<WebElement> getPossibleNodesOptions() {
        return allowedNodes.resolve().findElements(by.tagName("option"));
    }

    @Override
    public void fillWith(Object v) {
        for (String l : v.toString().split(",[ ]?")) {
            control("value").select(l);
        }
    }

    public List<String> applicableNodes() {
        List<String> nodes = new ArrayList<>();
        for (WebElement slave: control("value").resolve().findElements(by.tagName("option"))) {
            nodes.add(slave.getText());
        }
        return nodes;
    }
}
