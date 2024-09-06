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

    public <Ret extends BuildParameters> Ret addParameter(final Class<Ret> type) {
        String path = createPageArea(
                "configs", () -> control("hetero-list-add[configs]").selectDropdownMenu(type));
        return newInstance(type, this, path);
    }
}
