package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Groovy parser configuration in Jenkins global system configuration page.
 *
 * @author Ullrich Hafner
 */
public class GroovyParser extends PageAreaImpl {
    public static final String LINK_SUFFIX = "-link";

    private Control name = control("name");
    private Control linkName = control("linkName");
    private Control trendName = control("trendName");
    private Control regexp = control("regexp");
    private Control script = control("script");
    private Control example = control("example");

    /**
     * Creates a new Groovy parser page area.
     *
     * @param container the page area that contains this parser
     * @param path      the path in the page
     */
    public GroovyParser(final PageObject container, final String path) {
        super(container, path);
    }

    /**
     * Sets the properties of this parser.
     *
     * @param name   the name of the parser
     * @param script the mapping script
     */
    public void setScript(final String name, final String script) {
        this.name.set(name);
        this.trendName.set(name + "-trend");
        this.linkName.set(name + LINK_SUFFIX);
        this.script.set(script);
        this.regexp.set("(.*)");
        this.example.set("Line that matches the regular expression.");
    }
}
