package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Parser configuration in Jenkins global system configuration page.
 * Basically this page area handles only adding and removing of parsers. Each parser
 * is configured by an individual page area, see {@link GroovyParser} for details.
 *
 * @author Ullrich Hafner
 */
public class ParsersConfiguration extends PageAreaImpl {
    private static final String PARSERS = "parsers";
    private Control parsers = findRepeatableAddButtonFor(PARSERS);

    /**
     * Creates a new parsers configuration page area.
     *
     * @param context Jenkins global configuration page
     */
    public ParsersConfiguration(final JenkinsConfig context) {
        super(context, "/hudson-plugins-warnings-WarningsPublisher");
    }

    /**
     * Returns the repeatable add button for the specified property.
     *
     * @param propertyName the name of the repeatable property
     * @return the selected repeatable add button
     */
    // TODO: pull up (see warnings page object)
    private Control findRepeatableAddButtonFor(final String propertyName) {
        return control(by.xpath("//div[@id='" + propertyName + "']//button[contains(@path,'repeatable-add')]"));
    }

    public void add(final String name, final String script) {
        String path = createPageArea(PARSERS, () -> parsers.click());
        GroovyParser parser = new GroovyParser(getPage(), path);
        parser.setScript(name, script);
    }
}
