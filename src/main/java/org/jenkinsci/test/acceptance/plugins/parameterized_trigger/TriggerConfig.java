package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * @author Kohsuke Kawaguchi
 */
public class TriggerConfig extends PageAreaImpl {
    public final Control projects = control("projects");
    public final Control block = control("block");

    public TriggerConfig(ParameterizedTrigger parent, String relativePath) {
        super(parent, relativePath);
    }

    public <Ret extends BuildParameters> Ret addParameter(Class<Ret> type) {
        control("hetero-list-add[configs]").selectDropdownMenu(type);
        String path = last(by.xpath("//div[@name='configs']")).getAttribute("path");
        return newInstance(type, this, path);
    }
}
